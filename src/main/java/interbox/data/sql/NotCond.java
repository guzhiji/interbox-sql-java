package interbox.data.sql;


class NotCond extends LogicalCond {
    NotCond(QbCondClause cond) {
        super(QueryBuilder.Logical.NOT, cond, null);
    }
}
