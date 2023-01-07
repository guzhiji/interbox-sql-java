package interbox.data.sql;

import java.io.Serializable;
import java.util.function.Function;


@FunctionalInterface
interface SerializedFunction<T, R> extends Function<T, R>, Serializable {
}
