package interbox.data.sql;


class NotCond extends RelCond {
    NotCond(QbCondClause cond) {
        super(QueryBuilder.Rel.NOT, cond, null);
    }
}
