package interbox.data.sql;


class InSubqueryCond extends QbCondClause {
    final String expr;
    final QbSelect subquery;

    InSubqueryCond(String expr, QbSelect table) {
        this.expr = expr;
        this.subquery = table;
    }
}
