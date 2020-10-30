package interbox.data.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class QbSelect {

    boolean distinct = false;
    final List<FieldExpr> fields = new ArrayList<>();
    final String fromTable;
    final QbSelect fromSubquery;
    final String fromAlias;
    final List<JoinClause> joins = new ArrayList<>();
    QbCondClause where;
    final List<GroupByClause> groupBys = new ArrayList<>();
    final List<OrderBy> orderBys = new ArrayList<>();

    QbSelect(String tableName, String tableAlias) {
        this.fromTable = tableName;
        this.fromAlias = tableAlias;
        this.fromSubquery = null;
    }

    QbSelect(QbSelect table, String tableAlias) {
        this.fromSubquery = table;
        this.fromAlias = tableAlias;
        this.fromTable = null;
    }

    public QbSelect distinct() {
        this.distinct = true;
        return this;
    }

    public QbSelect field(String fieldExpr) {
        return field(fieldExpr, null);
    }

    public QbSelect field(String fieldExpr, String fieldAlias) {
        fields.add(new FieldExpr(fieldExpr, fieldAlias));
        return this;
    }

    public QbSelect innerJoin(String table2, QbCondClause on) {
        return innerJoin(table2, null, on);
    }

    public QbSelect innerJoin(String table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        joins.add(new JoinClause(JoinClause.Type.INNER, table2, alias, on));
        return this;
    }

    public QbSelect innerJoin(QbSelect table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        Objects.requireNonNull(alias);
        joins.add(new JoinClause(JoinClause.Type.INNER, table2, alias, on));
        return this;
    }

    public QbSelect groupBy(String field) {
        return groupBy(field, null);
    }

    public QbSelect groupBy(String field, QbCondClause having) {
        Objects.requireNonNull(field);
        groupBys.add(new GroupByClause(field, having));
        return this;
    }

    public QbSelect orderBy(String field) {
        return orderBy(field, null);
    }

    public QbSelect orderBy(String field, QueryBuilder.Order order) {
        Objects.requireNonNull(field);
        orderBys.add(new OrderBy(field, order));
        return this;
    }

    public QbSelect where(QbCondClause cond) {
        if (this.where == null) {
            this.where = cond;
        } else {
            this.where = this.where.and(cond);
        }
        return this;
    }

    //-------------------------------------

    private GenCtx genSql() {
        List<Object> params = new ArrayList<>();
        GenCtx genCtx = new GenCtx(params);
        SelectGen gen = new SelectGen();
        gen.visitSelect(this, genCtx);
        return genCtx;
    }

    public boolean exists(DataSource ds) {
        try {
            return exists(ds.getConnection());
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public boolean exists(Connection conn) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            for (int i = 0; i < genCtx.params.size(); i++) {
                Object p = genCtx.params.get(i);
                stmt.setObject(i + 1, p);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

    public <T> T findOne(DataSource ds, Class<T> cls) {
        try {
            return findOne(ds.getConnection(), cls);
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public <T> T findOne(Connection conn, Class<T> cls) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            for (int i = 0; i < genCtx.params.size(); i++) {
                Object p = genCtx.params.get(i);
                stmt.setObject(i + 1, p);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                return null; // TODO data mapping
            }
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

    public <T> List<T> findAll(DataSource ds, Class<T> cls) {
        try {
            return findAll(ds.getConnection(), cls);
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public <T> List<T> findAll(Connection conn, Class<T> cls) {
        return Collections.emptyList();
    }

}
