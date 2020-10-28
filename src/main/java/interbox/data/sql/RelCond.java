package interbox.data.sql;


class RelCond extends QbCondClause {
    final QueryBuilder.Rel rel;
    final QbCondClause cond1;
    final QbCondClause cond2;

    RelCond(QueryBuilder.Rel rel, QbCondClause cond1, QbCondClause cond2) {
        this.rel = rel;
        this.cond1 = cond1;
        this.cond2 = cond2;
    }
}
