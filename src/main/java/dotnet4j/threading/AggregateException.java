// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package dotnet4j.threading;

import java.util.List;
import java.util.concurrent.CompletionException;


public class AggregateException extends CompletionException {

    public AggregateException(List<Throwable> causes) {
        super(causes.get(0));
    }
}