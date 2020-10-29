package interbox.data.sql;


class LogicalCond extends QbCondClause {
    final QueryBuilder.Logical logical;
    final QbCondClause cond1;
    final QbCondClause cond2;

    LogicalCond(QueryBuilder.Logical logical, QbCondClause cond1, QbCondClause cond2) {
        this.logical = logical;
        this.cond1 = cond1;
        this.cond2 = cond2;
    }
}
