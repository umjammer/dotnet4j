// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

public interface Awaitable<T> {
    Awaiter<T> getAwaiter();
}
