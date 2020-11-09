package interbox.data.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class QbUpdate {
    final String table;
    final Class<?> tableClass;
    final List<Assignment> assignments = new ArrayList<>();
    QbCondClause where;

    QbUpdate(String tableName) {
        Objects.requireNonNull(tableName);
        this.table = tableName;
        this.tableClass = null;
    }

    QbUpdate(String tableName, Class<?> tableClass) {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(tableClass);
        this.table = tableName;
        this.tableClass = tableClass;
    }

    QbUpdate(Class<?> tableClass) {
        Objects.requireNonNull(tableClass);
        this.tableClass = tableClass;
        this.table = Utils.getTableName(tableClass);
        if (this.table == null)
            throw new QbException("no Table annotation found on the table class");
    }

    public QbUpdate value(String field, Object value, int type) {
        assignments.add(new OAssign(field, value, type));
        return this;
    }

    public QbUpdate value(String field, Object value) {
        int type = 0;
        if (this.tableClass != null) {
            type = Utils.getFieldType(this.tableClass, field);
        } else if (value != null) {
            type = Utils.inferType(value.getClass());
        }
        if (type == 0)
            throw new QbException("cannot determine value type");
        assignments.add(new OAssign(field, value, type));
        return this;
    }

    public QbUpdate values(Map<String, ?> values) {
        for (Map.Entry<String, ?> e : values.entrySet()) {
            value(e.getKey(), e.getValue());
        }
        return this;
    }

    public QbUpdate values(Object obj) {
        Objects.requireNonNull(obj);
        assignments.addAll(Utils.objectToAssignments(obj));
        return this;
    }

    public QbUpdate expr(String field, String expr) {
        assignments.add(new SAssign(field, expr));
        return this;
    }

    public QbUpdate where(QbCondClause cond) {
        if (this.where == null) {
            this.where = cond;
        } else {
            this.where = this.where.and(cond);
        }
        return this;
    }

    //-------------------------------------

    private GenCtx genSql() {
        GenCtx genCtx = new GenCtx();
        SqlGen gen = new SqlGen();
        gen.visitUpdate(this, genCtx);
        return genCtx;
    }

    public int execute(DataSource ds) {
        try {
            return execute(ds.getConnection());
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public int execute(Connection conn) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            Utils.setStmtParams(stmt, genCtx);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

}
