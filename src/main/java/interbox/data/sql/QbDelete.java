package interbox.data.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;


public final class QbDelete {
    private final static Logger log = LoggerFactory.getLogger(QbDelete.class);
    final String fromTable;
    QbCondClause where;

    QbDelete(String tableName) {
        Objects.requireNonNull(tableName);
        fromTable = tableName;
    }

    QbDelete(Class<?> tableClass) {
        Objects.requireNonNull(tableClass);
        fromTable = Utils.getTableName(tableClass);
        if (fromTable == null)
            throw new QbException("no Table annotation found on the table class");
    }

    public QbDelete where(QbCondClause cond) {
        if (this.where == null) {
            this.where = cond;
        } else {
            this.where = this.where.and(cond);
        }
        return this;
    }

    private GenCtx genSql() {
        GenCtx genCtx = new GenCtx();
        SqlGen gen = new SqlGen();
        gen.visitDelete(this, genCtx);
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
            for (int i = 0; i < genCtx.params.size(); i++) {
                Object p = genCtx.params.get(i).value;
                stmt.setObject(i + 1, p);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }
}
