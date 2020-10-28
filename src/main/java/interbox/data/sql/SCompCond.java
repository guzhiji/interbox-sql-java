package interbox.data.sql;


class SCompCond extends QbCondClause {
    final QueryBuilder.Comp comp;
    final String expr;
    final String str;
    final boolean asExpr;

    SCompCond(QueryBuilder.Comp comp, String expr, String str, boolean asExpr) {
        this.comp = comp;
        this.expr = expr;
        this.str = str;
        this.asExpr = asExpr;
    }

}
