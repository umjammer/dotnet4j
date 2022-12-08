// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;


public final class CancellationToken {

    private static final CancellationToken NONE = new CancellationToken();

    private final WeakReference<CancellationTokenSource> weakSource;

    private CancellationToken() {
        this.weakSource = new WeakReference<>(null);
    }

    CancellationToken(CancellationTokenSource source) {
        this.weakSource = new WeakReference<>(source);
    }

    public static CancellationToken none() {
        return NONE;
    }

    public boolean canBeCancelled() {
        CancellationTokenSource source = weakSource.get();
        return source != null && !source.isClosed();
    }

    public boolean isCancellationRequested() {
        CancellationTokenSource source = weakSource.get();
        return source != null && source.isCancellationRequested();
    }

    public void throwIfCancellationRequested() throws CancellationException {
        if (isCancellationRequested()) {
            throw new CancellationException();
        }
    }

    public CancellationTokenRegistration register(Runnable runnable) {
        return register(runnable, false);
    }

    public CancellationTokenRegistration register(Runnable runnable, boolean useSynchronizationContext) {
        return register(Runnable::run, runnable, useSynchronizationContext);
    }

    public <T> CancellationTokenRegistration register(Consumer<T> callback, T state) {
        return register(callback, state, false);
    }

    public <T> CancellationTokenRegistration register(Consumer<T> callback, T state, boolean useSynchronizationContext) {
        CancellationTokenSource source = weakSource.get();
        if (source == null) {
            return CancellationTokenRegistration.none();
        }

        return source.register(callback, state, useSynchronizationContext);
    }
}
