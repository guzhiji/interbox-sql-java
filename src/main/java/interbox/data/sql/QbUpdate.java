package interbox.data.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class QbUpdate {
    final String table;
    final List<Assignment> assignments = new ArrayList<>();
    QbCondClause where;

    QbUpdate(String tableName) {
        Objects.requireNonNull(tableName);
        table = tableName;
    }

    QbUpdate(String tableName, Class<?> tableClass) {
        Objects.requireNonNull(tableClass);
        table = tableName;
        // TODO collect type info about each field
    }

    QbUpdate(Class<?> tableClass) {
        Objects.requireNonNull(tableClass);
        table = null; // TODO get table name
        // TODO collect type info about each field
    }

    public QbUpdate value(String field, Object value, int type) {
        assignments.add(new OAssign(field, value, type));
        return this;
    }

    public QbUpdate value(String field, Object value) {

        return this;
    }

    public QbUpdate values(Map<String, ?> values) {

        return this;
    }

    public QbUpdate values(Object obj) {

        return this;
    }

    public <T> QbUpdate values(T obj, Class<T> clz) {

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
            for (int i = 0; i < genCtx.params.size(); i++) {
                Param p = genCtx.params.get(i);
                if (p.type == null)
                    stmt.setObject(i + 1, p.value);
                else
                    stmt.setObject(i + 1, p.value, p.type);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

}
