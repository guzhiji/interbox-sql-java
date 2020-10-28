package interbox.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


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

    public int execute(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement("")) {
            // TODO set params
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }
}
