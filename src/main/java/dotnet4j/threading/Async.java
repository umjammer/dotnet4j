// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


public enum Async {
    ;

    static final boolean REQUIRE_UNWRAP_FOR_COMPLETED_ANTECEDENT;

    static {
        // The behavior of CompletableFuture.thenCompose when the antecedent is
        // already completed is not consistent
        // across Java 8 releases. We are only allowed to avoid a call to
        // Futures.unwrap in this case if the current
        // runtime properly propagates cancellation of the continuation.
        CompletableFuture<Void> composed = Futures.completedNull().thenCompose(s -> Futures.completedCancelled());
        boolean behavedCorrectly = composed.isDone() && composed.isCancelled();
        REQUIRE_UNWRAP_FOR_COMPLETED_ANTECEDENT = !behavedCorrectly;
    }

    private static final ScheduledExecutorService DELAY_SCHEDULER = Executors.newSingleThreadScheduledExecutor((Runnable r) -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName(thread.getName() + " delayAsync scheduler");
        return thread;
    });

    public static <T> CompletableFuture<T> awaitAsync(Awaitable<? extends T> awaitable) {
        return awaitAsync(awaitable, AsyncFunctions.identity());
    }

    public static <T, U> CompletableFuture<U> awaitAsync(Awaitable<? extends T> awaitable,
                                                         Function<? super T, ? extends CompletableFuture<U>> continuation) {
        Awaiter<? extends T> awaiter = awaitable.getAwaiter();
        if (awaiter.isDone()) {
            try {
                return continuation.apply(awaiter.getResult());
            } catch (Throwable ex) {
                return Futures.fromException(ex);
            }
        }

        Executor executor;
        if (awaiter instanceof CriticalNotifyCompletion) {
            executor = ((CriticalNotifyCompletion) awaiter)::unsafeOnCompleted;
        } else {
            executor = awaiter::onCompleted;
        }

        final Supplier<? extends CompletableFuture<U>> flowContinuation = ExecutionContext
                .wrap(() -> continuation.apply(awaiter.getResult()));
        return Futures.supplyAsync(flowContinuation, executor);
    }

    public static <U> CompletableFuture<U> awaitAsync(Awaitable<?> awaitable,
                                                      Supplier<? extends CompletableFuture<U>> continuation) {
        return awaitAsync(awaitable, ignored -> continuation.get());
    }

    public static <T> CompletableFuture<T> awaitAsync(CompletableFuture<? extends T> future) {
        return awaitAsync(future, AsyncFunctions.identity());
    }

    public static <T, U> CompletableFuture<U> awaitAsync(CompletableFuture<? extends T> future,
                                                         Function<? super T, ? extends CompletableFuture<U>> continuation) {
        if (!REQUIRE_UNWRAP_FOR_COMPLETED_ANTECEDENT && future.isDone()) {
            // When the antecedent is already complete, we don't need to use
            // unwrap in order for cancellation to be
            // properly handled.
            return future.thenCompose(continuation);
        }

        Awaitable<? extends T> awaitable = new FutureAwaitable<>(future, true);
        return awaitAsync(awaitable, continuation);
    }

    public static <U> CompletableFuture<U> awaitAsync(CompletableFuture<?> future,
                                                      Supplier<? extends CompletableFuture<U>> continuation) {
        if (!REQUIRE_UNWRAP_FOR_COMPLETED_ANTECEDENT && future.isDone()) {
            // When the antecedent is already complete, we don't need to use
            // unwrap in order for cancellation to be
            // properly handled.
            return future.thenCompose(result -> continuation.get());
        }

        return awaitAsync(new FutureAwaitable<>(future, true), continuation);
    }

    public static CompletableFuture<Void> awaitAsync(Executor executor) {
        return awaitAsync(AwaitExtensions.switchTo(executor), AsyncFunctions.identity());
    }

    public static <U> CompletableFuture<U> awaitAsync(Executor executor,
                                                      Supplier<? extends CompletableFuture<U>> continuation) {
        Function<Void, CompletableFuture<U>> function = ignored -> continuation.get();
        return awaitAsync(AwaitExtensions.switchTo(executor), function);
    }

    public static <T> Awaitable<T> configureAwait(CompletableFuture<? extends T> future, boolean continueOnCapturedContext) {
        return new FutureAwaitable<>(future, continueOnCapturedContext);
    }

    public static <T> CompletableFuture<T> runAsync(Supplier<? extends CompletableFuture<T>> supplier) {
        try {
            StrongBox<CompletableFuture<T>> result = new StrongBox<>();
            ExecutionContext.run(ExecutionContext.capture(), s -> result.value = s.get(), supplier);
            return result.value;
        } catch (Throwable ex) {
            return Futures.fromException(ex);
        }
    }

    public static CompletableFuture<Void> delayAsync(Duration duration) {
        return delayAsync(duration, CancellationToken.none());
    }

    public static CompletableFuture<Void> delayAsync(Duration duration, CancellationToken cancellationToken) {
        if (cancellationToken.isCancellationRequested()) {
            return Futures.completedCancelled();
        }

        if (duration.isZero()) {
            return Futures.completedNull();
        }

        CompletableFuture<Void> result = new CompletableFuture<>();
        ScheduledFuture<?> scheduled = DELAY_SCHEDULER.schedule(ExecutionContext.wrap(() -> ForkJoinPool.commonPool().execute(ExecutionContext.wrap(() -> {
            result.complete(null);
        }))), duration.toMillis(), TimeUnit.MILLISECONDS);

        // Unschedule if cancelled
        result.whenComplete((ignored, exception) -> {
            if (result.isCancelled()) {
                scheduled.cancel(true);
            }
        });

        if (cancellationToken.canBeCancelled()) {
            CancellationTokenRegistration registration = cancellationToken.register(f -> f.cancel(true), result);
            result.whenComplete((ignored, exception) -> registration.close());
        }

        return result;
    }

    public static <T> CompletableFuture<T> finallyAsync(CompletableFuture<T> future, Runnable runnable) {
        // When both future and runnable throw an exception, the semantics of a
        // finally block give precedence to the
        // exception thrown by the finally block. However, the implementation of
        // CompletableFuture.whenComplete gives
        // precedence to the future.
        return Futures.unwrap(future.handle((result, exception) -> {
            runnable.run();
            return future;
        }));
    }

    public static <U> CompletableFuture<U> usingAsync(AutoCloseable resource, Supplier<? extends CompletableFuture<U>> body) {
        return usingAsync(resource, r -> body.get());
    }

    public static <T extends AutoCloseable, U> CompletableFuture<U> usingAsync(T resource,
                                                                               Function<? super T, ? extends CompletableFuture<U>> body) {
        CompletableFuture<U> evaluatedBody;
        try {
            evaluatedBody = body.apply(resource);
        } catch (Throwable ex) {
            evaluatedBody = Futures.fromException(ex);
        }

        return finallyAsync(evaluatedBody, () -> {
            try {
                resource.close();
            } catch (CompletionException | CancellationException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new CompletionException(ex);
            }
        });
    }

    public static CompletableFuture<Void> forAsync(Runnable initializer,
                                                   Supplier<? extends Boolean> condition,
                                                   Runnable increment,
                                                   Supplier<? extends CompletableFuture<?>> body) {
        try {
            initializer.run();
            return whileAsync(condition, () -> body.get().thenRun(increment));
        } catch (Throwable t) {
            return Futures.completedFailed(t);
        }
    }

    public static <T> CompletableFuture<Void> forAsync(Supplier<? extends T> initializer,
                                                       Predicate<? super T> condition,
                                                       Function<? super T, ? extends T> increment,
                                                       Function<? super T, ? extends CompletableFuture<?>> body) {
        AtomicReference<T> value = new AtomicReference<>();
        return forAsync(() -> value.set(initializer.get()),
                        () -> condition.test(value.get()),
                        () -> value.set(increment.apply(value.get())),
                        () -> body.apply(value.get()));
    }

    public static <T> CompletableFuture<Void> forAsync(Supplier<? extends T> initializer,
                                                       Predicate<? super T> condition,
                                                       Function<? super T, ? extends T> increment,
                                                       Supplier<? extends CompletableFuture<?>> body) {
        @SuppressWarnings("unused")
        AtomicReference<T> value = new AtomicReference<>();
        return forAsync(initializer, condition, increment, ignored -> body.get());
    }

    public static CompletableFuture<Void> whileAsync(Supplier<? extends Boolean> predicate,
                                                     Supplier<? extends CompletableFuture<?>> body) {
        try {
            if (!predicate.get()) {
                return Futures.completedNull();
            }

            final ConcurrentLinkedQueue<Supplier<CompletableFuture<?>>> futures = new ConcurrentLinkedQueue<>();
            final AtomicReference<Supplier<CompletableFuture<?>>> evaluateBody = new AtomicReference<>();
            evaluateBody.set(() -> {
                CompletableFuture<?> bodyResult = body.get();
                return bodyResult.thenRun(() -> {
                    if (predicate.get()) {
                        futures.add(evaluateBody.get());
                    }
                });
            });

            futures.add(evaluateBody.get());
            return whileImplAsync(futures);
        } catch (Throwable ex) {
            return Futures.completedFailed(ex);
        }
    }

    public static Awaitable<Void> yieldAsync() {
        return YieldAwaitable.INSTANCE;
    }

    private static CompletableFuture<Void> whileImplAsync(ConcurrentLinkedQueue<Supplier<CompletableFuture<?>>> futures) {
        while (true) {
            Supplier<CompletableFuture<?>> next = futures.poll();
            if (next == null) {
                return Futures.completedNull();
            }

            CompletableFuture<?> future = next.get();
            if (!future.isDone()) {
                return awaitAsync(future, () -> whileImplAsync(futures));
            }
        }
    }

    public static CompletableFuture<CompletableFuture<?>> whenAny(CompletableFuture<?>... futures) {
        return CompletableFuture.anyOf(futures).handle((result, exception) -> {
            for (CompletableFuture<?> future : futures) {
                if (future.isDone()) {
                    return future;
                }
            }

            throw new IllegalStateException("Expected at least one future to be complete.");
        });
    }

    private static final class YieldAwaitable implements Awaitable<Void> {
        public static final YieldAwaitable INSTANCE = new YieldAwaitable();

        @Override
        public Awaiter<Void> getAwaiter() {
            return YieldAwaiter.INSTANCE;
        }
    }

    private static final class YieldAwaiter implements Awaiter<Void>, CriticalNotifyCompletion {
        public static final YieldAwaiter INSTANCE = new YieldAwaiter();

        @Override
        public boolean isDone() {
            // yielding is always required for YieldAwaiter, hence false
            return false;
        }

        @Override
        public Void getResult() {
            return null;
        }

        @Override
        public void onCompleted(Runnable continuation) {
            onCompletedImpl(continuation, true);
        }

        @Override
        public void unsafeOnCompleted(Runnable continuation) {
            onCompletedImpl(continuation, false);
        }

        private void onCompletedImpl(Runnable continuation, boolean useExecutionContext) {
            Objects.nonNull(continuation);

            Executor executor = ForkJoinPool.commonPool();
            SynchronizationContext synchronizationContext = SynchronizationContext.getCurrent();
            if (synchronizationContext != null && synchronizationContext.getClass() != SynchronizationContext.class) {
                executor = synchronizationContext;
            }

            Runnable wrappedContinuation = useExecutionContext ? ExecutionContext.wrap(continuation) : continuation;
            executor.execute(wrappedContinuation);
        }
    }
}
