// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class SynchronizationContext implements Executor {

    private boolean waitNotificationRequired;

    public static SynchronizationContext getCurrent() {
        return ThreadProperties.getSynchronizationContext(Thread.currentThread());
    }

    public static void setSynchronizationContext(SynchronizationContext synchronizationContext) {
        ThreadProperties.setSynchronizationContext(Thread.currentThread(), synchronizationContext);
    }

    public SynchronizationContext createCopy() {
        return new SynchronizationContext();
    }

    public final boolean isWaitNotificationRequired() {
        return waitNotificationRequired;
    }

    public void operationStarted() {
    }

    public void operationCompleted() {
    }

    public <T> void post(Consumer<T> callback, T state) {
        Futures.runAsync(() -> callback.accept(state));
    }

    public <T> void send(Consumer<T> callback, T state) {
        callback.accept(state);
    }

    protected final void setWaitNotificationRequired() {
        waitNotificationRequired = true;
    }

    @Override
    public void execute(Runnable command) {
        post(Runnable::run, command);
    }
}
