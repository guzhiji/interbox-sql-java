package interbox.data.sql;


class InCond extends QbCondClause {
    final String expr;
    final QbSelect subquery;

    InCond(String expr, QbSelect table) {
        this.expr = expr;
        this.subquery = table;
    }
}
