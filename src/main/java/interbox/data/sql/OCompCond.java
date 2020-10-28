package interbox.data.sql;


class OCompCond extends QbCondClause {
    final QueryBuilder.Comp comp;
    final String expr;
    final Object value;

    OCompCond(QueryBuilder.Comp comp, String expr, Object value) {
        this.comp = comp;
        this.expr = expr;
        this.value = value;
    }
}
