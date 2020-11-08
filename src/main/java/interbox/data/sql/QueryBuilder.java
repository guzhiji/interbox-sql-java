package interbox.data.sql;

import java.util.*;


public final class QueryBuilder {

    public enum Order {
        ASC, DESC
    }

    public enum Comp {
        EQ("="), NEQ("<>"),
        LT("<"), LTE("<="),
        GT(">"), GTE(">="),
        LIKE("like"), NOT_LIKE("not like"),
        IN("in"), NOT_IN("not in");

        private final static Map<Comp, Comp> NEGATION_MAP;
        static {
            Map<Comp, Comp> m = new HashMap<>();
            m.put(EQ, NEQ);
            m.put(NEQ, EQ);
            m.put(LT, GTE);
            m.put(GTE, LT);
            m.put(GT, LTE);
            m.put(LTE, GT);
            m.put(LIKE, NOT_LIKE);
            m.put(NOT_LIKE, LIKE);
            m.put(IN, NOT_IN);
            m.put(NOT_IN, IN);
            NEGATION_MAP = Collections.unmodifiableMap(m);
        }

        final String token;
        Comp(String t) {
            token = t;
        }

        public Comp negate() {
            return NEGATION_MAP.get(this);
        }
    }

    public enum Logical {
        AND, OR, NOT
    }

    public static QbSelect selectFrom(String tableName, String tableAlias) {
        return new QbSelect(tableName, tableAlias);
    }

    public static QbSelect selectFrom(String tableName) {
        return new QbSelect(tableName, null);
    }

    public static QbSelect selectFrom(Class<?> tableClass, String tableAlias) {
        return new QbSelect(tableClass, tableAlias);
    }

    public static QbSelect selectFrom(Class<?> tableClass) {
        return new QbSelect(tableClass, null);
    }

    public static QbSelect selectFrom(QbSelect table, String tableAlias) {
        return new QbSelect(table, tableAlias);
    }

    public static QbDelete deleteFrom(String tableName) {
        return new QbDelete(tableName);
    }

    public static QbDelete deleteFrom(Class<?> tableClass) {
        return new QbDelete(tableClass);
    }

    public static QbUpdate update(String tableName) {
        return new QbUpdate(tableName);
    }

    public static QbUpdate update(String tableName, Class<?> tableClass) {
        return new QbUpdate(tableName, tableClass);
    }

    public static QbUpdate update(Class<?> tableClass) {
        return new QbUpdate(tableClass);
    }

    public static QbInsert insertInto(String tableName) {
        return new QbInsert(tableName);
    }

    public static QbInsert insertInto(String tableName, Class<?> tableClass) {
        return new QbInsert(tableName, tableClass);
    }

    public static QbInsert insertInto(Class<?> tableClass) {
        return new QbInsert(tableClass);
    }

    public static QbCondClause cond(String expr) {
        return new ExprCond(expr);
    }

    public static QbCondClause eq(String expr, Object value) {
        return new OCompCond(Comp.EQ, expr, value);
    }

    public static QbCondClause eq(String expr, String str) {
        return new SCompCond(Comp.EQ, expr, str, false);
    }

    public static QbCondClause eq(String expr, String str, boolean asExpr) {
        return new SCompCond(Comp.EQ, expr, str, asExpr);
    }

    public static QbCondClause compare(Comp comp, String expr, Object value) {
        return new OCompCond(comp, expr, value);
    }

    public static QbCondClause compare(Comp comp, String expr, String str) {
        return new SCompCond(comp, expr, str, false);
    }

    public static QbCondClause compare(Comp comp, String expr, String str, boolean asExpr) {
        return new SCompCond(comp, expr, str, asExpr);
    }

    public static QbCondClause in(String expr, QbSelect table2) {
        return new InCond(expr, table2);
    }

    public static QbCondClause exists(QbSelect table2) {
        return new ExistsCond(table2);
    }

    private static QbCondClause convCondList(Logical logical, List<QbCondClause> condList) {
        int n = condList.size();
        if (n == 0)
            throw new QbException("empty condition list");
        if (n < 2)
            return condList.get(0);
        if (n == 2)
            return new LogicalCond(logical, condList.get(0), condList.get(1));
        return new LogicalCondList(logical, condList);
    }

    private static QbCondClause convCondList(Logical logical, QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        if (condMore.length == 0)
            return new LogicalCond(logical, cond1, cond2);
        List<QbCondClause> condList = new ArrayList<>();
        condList.add(cond1);
        condList.add(cond2);
        condList.addAll(Arrays.asList(condMore));
        return new LogicalCondList(logical, condList);
    }

    public static QbCondClause and(List<QbCondClause> condList) {
        return convCondList(Logical.AND, condList);
    }

    public static QbCondClause and(QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        return convCondList(Logical.AND, cond1, cond2, condMore);
    }

    public static QbCondClause or(List<QbCondClause> condList) {
        return convCondList(Logical.OR, condList);
    }

    public static QbCondClause or(QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        return convCondList(Logical.OR, cond1, cond2, condMore);
    }

    public static QbCondClause not(QbCondClause cond) {
        return new NotCond(cond);
    }

}
