package interbox.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class QbDelete {
    final String fromTable;
    QbCondClause where;

    QbDelete(String tableName) {
        fromTable = tableName;
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
        List<Object> params = new ArrayList<>();
        GenCtx genCtx = new GenCtx(params);
        SelectGen gen = new SelectGen();
        gen.visitDelete(this, genCtx);
        return genCtx;
    }

    public int execute(Connection conn) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            for (int i = 0; i < genCtx.params.size(); i++) {
                Object p = genCtx.params.get(i);
                stmt.setObject(i + 1, p);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }
}
