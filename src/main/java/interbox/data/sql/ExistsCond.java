package interbox.data.sql;


class ExistsCond extends QbCondClause {
    final QbSelect subquery;

    ExistsCond(QbSelect table) {
        this.subquery = table;
    }
}
