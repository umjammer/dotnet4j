// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.util.function.Consumer;


/**
 * Stores references such that they are available for retrieval in the same call
 * context.
 *
 * <p>
 * Copied from Microsoft/vs-threading@14f77875.
 * </p>
 *
 * @param <T> The type of value to store.
 */
public final class AsyncLocal<T> {

    private final Consumer<? super AsyncLocalValueChangedEventArgs<T>> valueChangedHandler;

    public AsyncLocal() {
        this.valueChangedHandler = null;
    }

    public AsyncLocal(Consumer<? super AsyncLocalValueChangedEventArgs<T>> valueChangedHandler) {
        this.valueChangedHandler = valueChangedHandler;
    }

    public T getValue() {
        return ExecutionContext.getLocalValue(this);
    }

    public void setValue(T value) {
        ExecutionContext.setLocalValue(this, value, valueChangedHandler != null);
    }

    public void onValueChanged(T previousValue, T currentValue, boolean contextChanged) {
        assert valueChangedHandler != null;

        valueChangedHandler.accept(new AsyncLocalValueChangedEventArgs<>(previousValue, currentValue, contextChanged));
    }
}
