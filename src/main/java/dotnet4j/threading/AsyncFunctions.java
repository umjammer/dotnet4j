// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


enum AsyncFunctions {
    ;

    private static final Function<Object, CompletableFuture<?>> IDENTITY = CompletableFuture::completedFuture;

    public static <T> Function<T, CompletableFuture<T>> identity() {
        @SuppressWarnings("unchecked")
        Function<T, CompletableFuture<T>> result = (Function<T, CompletableFuture<T>>) (Object) IDENTITY;
        return result;
    }
}
