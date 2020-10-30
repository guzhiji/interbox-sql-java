package interbox.data.sql;


class SAssign extends Assignment {
    final String expr;

    SAssign(String field, String expr) {
        super(field);
        this.expr = expr;
    }
}
