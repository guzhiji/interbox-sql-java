package interbox.data.sql;


class OAssign extends Assignment {
    final Object value;
    final int type;

    OAssign(String field, Object value, int type) {
        super(field);
        this.value = value;
        this.type = type;
    }
}
