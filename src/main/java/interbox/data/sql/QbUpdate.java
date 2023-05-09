package interbox.data.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class QbUpdate {
    private final static Logger log = LoggerFactory.getLogger(QbUpdate.class);
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
            throw new QbException("no Table annotation found on the given table class");
    }

    public QbUpdate value(String field, Object value, int type) {
        assignments.add(new OAssign(field, value, type));
        return this;
    }

    public <E, R> QbUpdate value(SerializedFunction<E, R> methodRef, Object value, int type) {
        return value(Utils.getTableFieldName(methodRef), value, type);
    }

    public QbUpdate value(String field, Object value) {
        int type;
        if (this.tableClass != null) {
            type = Utils.getFieldType(this.tableClass, field);
        } else if (value != null) {
            type = Utils.inferType(value.getClass());
        } else {
            type = 0; // null
        }
        assignments.add(new OAssign(field, value, type));
        return this;
    }

    public <E, R> QbUpdate value(SerializedFunction<E, R> methodRef, Object value) {
        return value(Utils.getTableFieldName(methodRef), value);
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

    public <E, R> QbUpdate expr(SerializedFunction<E, R> methodRef, String expr) {
        return expr(Utils.getTableFieldName(methodRef), expr);
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
        if (log.isDebugEnabled()) Utils.logGenCtx(log, genCtx);
        return genCtx;
    }

    public int execute(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            return execute(conn);
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
