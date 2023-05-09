package interbox.data.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class QbSelect {

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
        Objects.requireNonNull(tableName);
        this.fromTable = tableName;
        this.fromAlias = tableAlias;
        this.fromSubquery = null;
    }

    QbSelect(Class<?> tableClass, String tableAlias) {
        Objects.requireNonNull(tableClass);
        this.fromTable = Utils.getTableName(tableClass);
        if (this.fromTable == null)
            throw new QbException("no Table annotation found on the table class");
        this.fromAlias = tableAlias;
        this.fromSubquery = null;
    }

    QbSelect(QbSelect table, String tableAlias) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(tableAlias);
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

    public <E, R> QbSelect field(SerializedFunction<E, R> methodRef) {
        return field(Utils.getTableFieldName(methodRef));
    }

    public QbSelect field(String fieldExpr, String fieldAlias) {
        fields.add(new FieldExpr(fieldExpr, fieldAlias));
        return this;
    }

    public <E, R> QbSelect field(SerializedFunction<E, R> methodRef, String fieldAlias) {
        return field(Utils.getTableFieldName(methodRef), fieldAlias);
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

    public QbSelect leftJoin(String table2, QbCondClause on) {
        return leftJoin(table2, null, on);
    }

    public QbSelect leftJoin(String table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        joins.add(new JoinClause(JoinClause.Type.LEFT, table2, alias, on));
        return this;
    }

    public QbSelect leftJoin(QbSelect table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        Objects.requireNonNull(alias);
        joins.add(new JoinClause(JoinClause.Type.LEFT, table2, alias, on));
        return this;
    }

    public QbSelect rightJoin(String table2, QbCondClause on) {
        return rightJoin(table2, null, on);
    }

    public QbSelect rightJoin(String table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        joins.add(new JoinClause(JoinClause.Type.RIGHT, table2, alias, on));
        return this;
    }

    public QbSelect rightJoin(QbSelect table2, String alias, QbCondClause on) {
        Objects.requireNonNull(table2);
        Objects.requireNonNull(alias);
        joins.add(new JoinClause(JoinClause.Type.RIGHT, table2, alias, on));
        return this;
    }

    public QbSelect groupBy(String field) {
        return groupBy(field, null);
    }

    public <E, R> QbSelect groupBy(SerializedFunction<E, R> methodRef) {
        return groupBy(Utils.getTableFieldName(methodRef));
    }

    public QbSelect groupBy(String field, QbCondClause having) {
        Objects.requireNonNull(field);
        groupBys.add(new GroupByClause(field, having));
        return this;
    }

    public <E, R> QbSelect groupBy(SerializedFunction<E, R> methodRef, QbCondClause having) {
        return groupBy(Utils.getTableFieldName(methodRef), having);
    }

    public QbSelect orderBy(String field) {
        return orderBy(field, null);
    }

    public <E, R> QbSelect orderBy(SerializedFunction<E, R> methodRef) {
        return orderBy(Utils.getTableFieldName(methodRef));
    }

    public QbSelect orderBy(String field, QueryBuilder.Order order) {
        Objects.requireNonNull(field);
        orderBys.add(new OrderBy(field, order));
        return this;
    }

    public <E, R> QbSelect orderBy(SerializedFunction<E, R> methodRef, QueryBuilder.Order order) {
        return orderBy(Utils.getTableFieldName(methodRef), order);
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
        GenCtx genCtx = new GenCtx();
        SqlGen gen = new SqlGen();
        gen.visitSelect(this, genCtx);
        return genCtx;
    }

    public boolean exists(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            return exists(conn);
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public boolean exists(Connection conn) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            Utils.setStmtParams(stmt, genCtx);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

    public <T> T findOne(DataSource ds, Class<T> cls) {
        try (Connection conn = ds.getConnection()) {
            return findOne(conn, cls);
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public <T> T findOne(Connection conn, Class<T> cls) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            Utils.setStmtParams(stmt, genCtx);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    throw new QbException.NoResult();
                if (Utils.inferType(cls) == 0)
                    return Utils.resultSetToObject(rs, cls);
                return Utils.resultSetFirstCol(rs, cls);
            }
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

    public <T> List<T> findAll(DataSource ds, Class<T> cls) {
        try (Connection conn = ds.getConnection()) {
            return findAll(conn, cls);
        } catch (SQLException ex) {
            throw new QbException("fail to connect to db", ex);
        }
    }

    public <T> List<T> findAll(Connection conn, Class<T> cls) {
        GenCtx genCtx = genSql();
        try (PreparedStatement stmt = conn.prepareStatement(genCtx.result)) {
            Utils.setStmtParams(stmt, genCtx);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> out = new ArrayList<>();
                if (Utils.inferType(cls) == 0) { // not a value, possibly an entity
                    while (rs.next())
                        out.add(Utils.resultSetToObject(rs, cls));
                } else {
                    while (rs.next())
                        out.add(Utils.resultSetFirstCol(rs, cls));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new QbException("sql error", e);
        }
    }

}
