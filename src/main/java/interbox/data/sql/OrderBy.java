package interbox.data.sql;


class OrderBy {
    final String field;
    final QueryBuilder.Order order;

    OrderBy(String field) {
        this.field = field;
        this.order = QueryBuilder.Order.ASC;
    }

    OrderBy(String field, QueryBuilder.Order order) {
        this.field = field;
        this.order = order == null ? QueryBuilder.Order.ASC : order;
    }
}
