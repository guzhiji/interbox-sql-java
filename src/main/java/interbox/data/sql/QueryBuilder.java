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
        LIKE("like"), NOT_LIKE("not like");

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

    /**
     * free-style condition.
     *
     * @param expr      a sql expression
     * @return
     */
    public static QbCondClause cond(String expr) {
        return new ExprCond(expr);
    }

    /**
     * operator: equals (for an object value).
     *
     * @param expr      typically a field name
     * @param value     the object value
     * @return
     */
    public static QbCondClause eq(String expr, Object value) {
        return new OCompCond(Comp.EQ, expr, value);
    }

    /**
     * operator: equals (for an object value, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param value         the object value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause eq(SerializedFunction<E, R> methodRef, Object value) {
        return eq(Utils.getTableFieldName(methodRef), value);
    }

    /**
     * operator: not equals (for an object value).
     *
     * @param expr      typically a field name
     * @param value     the object value
     * @return
     */
    public static QbCondClause notEq(String expr, Object value) {
        return new OCompCond(Comp.NEQ, expr, value);
    }

    /**
     * operator: not equals (for an object value, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param value         the object value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause notEq(SerializedFunction<E, R> methodRef, Object value) {
        return notEq(Utils.getTableFieldName(methodRef), value);
    }

    /**
     * operator: equals (for a string value).
     *
     * @param expr      typically a field name
     * @param str       the string value
     * @return
     */
    public static QbCondClause eq(String expr, String str) {
        return new SCompCond(Comp.EQ, expr, str, false);
    }

    /**
     * operator: equals (for a string value, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param str           the string value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause eq(SerializedFunction<E, R> methodRef, String str) {
        return eq(Utils.getTableFieldName(methodRef), str);
    }

    /**
     * operator: not equals (for a string value).
     *
     * @param expr      typically a field name
     * @param str       the string value
     * @return
     */
    public static QbCondClause notEq(String expr, String str) {
        return new SCompCond(Comp.NEQ, expr, str, false);
    }

    /**
     * operator: not equals (for a string value, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param str           the string value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause notEq(SerializedFunction<E, R> methodRef, String str) {
        return notEq(Utils.getTableFieldName(methodRef), str);
    }

    /**
     * operator: equals (for an expression).
     *
     * @param expr      typically a field name
     * @param expr2     the expression
     * @return
     */
    public static QbCondClause eqExpr(String expr, String expr2) {
        return new SCompCond(Comp.EQ, expr, expr2, true);
    }

    /**
     * operator: equals (for an expression, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param expr          the expression
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause eqExpr(SerializedFunction<E, R> methodRef, String expr) {
        return eqExpr(Utils.getTableFieldName(methodRef), expr);
    }

    /**
     * operator: not equals (for an expression).
     *
     * @param expr      typically a field name
     * @param expr2     the expression
     * @return
     */
    public static QbCondClause notEqExpr(String expr, String expr2) {
        return new SCompCond(Comp.NEQ, expr, expr2, true);
    }

    /**
     * operator: not equals (for an expression, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param expr          the expression
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause notEqExpr(SerializedFunction<E, R> methodRef, String expr) {
        return notEqExpr(Utils.getTableFieldName(methodRef), expr);
    }

    /**
     * operator: like.
     *
     * @param expr      typically a field name
     * @param str       a string value
     * @return
     */
    public static QbCondClause like(String expr, String str) {
        return new SCompCond(Comp.LIKE, expr, str, false);
    }

    /**
     * operator: like (use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param str           a string value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause like(SerializedFunction<E, R> methodRef, String str) {
        return like(Utils.getTableFieldName(methodRef), str);
    }

    /**
     * operator: not like.
     *
     * @param expr      typically a field name
     * @param str       a string value
     * @return
     */
    public static QbCondClause notLike(String expr, String str) {
        return new SCompCond(Comp.NOT_LIKE, expr, str, false);
    }

    /**
     * operator: not like (use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param str           a string value
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause notLike(SerializedFunction<E, R> methodRef, String str) {
        return notLike(Utils.getTableFieldName(methodRef), str);
    }

    public static QbCondClause compare(Comp comp, String expr, Object value) {
        return new OCompCond(comp, expr, value);
    }

    public static <E, R> QbCondClause compare(Comp comp, SerializedFunction<E, R> methodRef, Object value) {
        return compare(comp, Utils.getTableFieldName(methodRef), value);
    }

    public static QbCondClause compare(Comp comp, String expr, String str) {
        return new SCompCond(comp, expr, str, false);
    }

    public static <E, R> QbCondClause compare(Comp comp, SerializedFunction<E, R> methodRef, String str) {
        return compare(comp, Utils.getTableFieldName(methodRef), str);
    }

    public static QbCondClause compareExpr(Comp comp, String expr, String expr2) {
        return new SCompCond(comp, expr, expr2, true);
    }

    public static <E, R> QbCondClause compareExpr(Comp comp, SerializedFunction<E, R> methodRef, String expr) {
        return compareExpr(comp, Utils.getTableFieldName(methodRef), expr);
    }

    /**
     * operator: in (for subquery).
     *
     * @param expr      typically a field name
     * @param table2    the subquery
     * @return
     */
    public static QbCondClause in(String expr, QbSelect table2) {
        return new InSubqueryCond(expr, table2);
    }

    /**
     * operator: in (for subquery, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param table2        the subquery
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause in(SerializedFunction<E, R> methodRef, QbSelect table2) {
        return in(Utils.getTableFieldName(methodRef), table2);
    }

    /**
     * operator: in (for a collection of values).
     *
     * @param expr      typically a field name
     * @param values    the collection of values
     * @return
     */
    public static QbCondClause in(String expr, Collection<Object> values) {
        return new InOArrayCond(expr, values);
    }

    /**
     * operator: in (for a collection of values, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param values        the collection of values
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause in(SerializedFunction<E, R> methodRef, Collection<Object> values) {
        return in(Utils.getTableFieldName(methodRef), values);
    }

    /**
     * operator: in (for an array of values).
     *
     * @param expr      typically a field name
     * @param values    the array of values
     * @return
     */
    public static QbCondClause in(String expr, Object... values) {
        return new InOArrayCond(expr, values);
    }

    /**
     * operator: in (for an array of values, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param values        the array of values
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause in(SerializedFunction<E, R> methodRef, Object... values) {
        return in(Utils.getTableFieldName(methodRef), values);
    }

    public static QbCondClause in(String expr, String... values) {
        return new InSArrayCond(expr, false, values);
    }

    public static <E, R> QbCondClause in(SerializedFunction<E, R> methodRef, String... values) {
        return in(Utils.getTableFieldName(methodRef), values);
    }

    /**
     * operator: in (for a collection of expressions).
     *
     * @param expr      typically a field name
     * @param exprs     the collection of expressions
     * @return
     */
    public static QbCondClause inExprs(String expr, Collection<String> exprs) {
        return new InSArrayCond(expr, true, exprs);
    }

    /**
     * operator: in (for a collection of expressions, use method reference syntax).
     *
     * @param methodRef     method reference for the desired field
     * @param exprs         the collection of expressions
     * @return
     * @param <E>           entity class
     * @param <R>           field data type
     */
    public static <E, R> QbCondClause inExprs(SerializedFunction<E, R> methodRef, Collection<String> exprs) {
        return inExprs(Utils.getTableFieldName(methodRef), exprs);
    }

    public static QbCondClause inExprs(String expr, String... exprs) {
        return new InSArrayCond(expr, true, exprs);
    }

    public static <E, R> QbCondClause inExprs(SerializedFunction<E, R> methodRef, String... exprs) {
        return inExprs(Utils.getTableFieldName(methodRef), exprs);
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
