package interbox.data.sql;


class GroupByClause {
    final String field;
    final QbCondClause having;

    GroupByClause(String field) {
        this.field = field;
        this.having = null;
    }

    GroupByClause(String field, QbCondClause having) {
        this.field = field;
        this.having = having;
    }

}
