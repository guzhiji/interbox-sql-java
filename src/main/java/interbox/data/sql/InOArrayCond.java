package interbox.data.sql;

import java.util.Arrays;
import java.util.List;


class InOArrayCond extends QbCondClause {
    final String expr;
    final List<Object> values;

    InOArrayCond(String expr, List<Object> values) {
        this.expr = expr;
        this.values = values;
    }

    InOArrayCond(String expr, Object... values) {
        this(expr, Arrays.asList(values));
    }
}
