package interbox.data.sql;


class FieldExpr {
    final String expr;
    final String alias;

    FieldExpr(String expr) {
        this.expr = expr;
        this.alias = null;
    }

    FieldExpr(String expr, String alias) {
        this.expr = expr;
        this.alias = alias;
    }

}
