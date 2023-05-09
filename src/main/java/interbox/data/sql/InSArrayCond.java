package interbox.data.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


class InSArrayCond extends QbCondClause {
    final String expr;
    final Collection<String> values;
    final boolean asExpr;

    InSArrayCond(String expr, boolean asExpr, Collection<String> values) {
        this.expr = expr;
        this.values = values;
        this.asExpr = asExpr;
    }

    InSArrayCond(String expr, boolean asExpr, String... values) {
        this(expr, asExpr, Arrays.asList(values));
    }
}
