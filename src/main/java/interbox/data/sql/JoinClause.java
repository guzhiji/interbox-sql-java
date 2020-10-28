package interbox.data.sql;


class JoinClause {
    enum Type {
        INNER, LEFT, RIGHT
    }

    final Type type;
    final String table;
    final QbSelect subquery;
    final String alias;
    final QbCondClause on;

    JoinClause(Type type, String table, String alias, QbCondClause on) {
        this.type = type;
        this.table = table;
        this.subquery = null;
        this.alias = alias;
        this.on = on;
    }

    JoinClause(Type type, QbSelect subquery, String alias, QbCondClause on) {
        this.type = type;
        this.table = null;
        this.subquery = subquery;
        this.alias = alias;
        this.on = on;
    }
}
