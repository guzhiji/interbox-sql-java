package interbox.data.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QueryBuilder {
    public enum Order {
        ASC, DESC
    }
    public enum Comp {
        EQ("="), NEQ("<>"), LT("<"), LTE("<="), GT(">"), GTE(">="), LIKE("like"), IN("in");

        final String token;
        Comp(String t) {
            token = t;
        }
    }
    public enum Rel {
        AND, OR, NOT
    }

    public static QbSelect selectFrom(String tableName, String tableAlias) {
        return new QbSelect(tableName, tableAlias);
    }

    public static QbSelect selectFrom(String tableName) {
        return new QbSelect(tableName, null);
    }

    public static QbSelect selectFrom(QbSelect table, String tableAlias) {
        return new QbSelect(table, tableAlias);
    }

    public static QbDelete deleteFrom(String tableName) {
        return new QbDelete(tableName);
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

    private static QbCondClause convCondList(Rel rel, List<QbCondClause> condList) {
        int n = condList.size();
        if (n == 0)
            throw new IllegalArgumentException("empty condition list");
        if (n < 2)
            return condList.get(0);
        if (n == 2)
            return new RelCond(rel, condList.get(0), condList.get(1));
        return new RelCondList(rel, condList);
    }

    private static QbCondClause convCondList(Rel rel, QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        if (condMore.length == 0)
            return new RelCond(rel, cond1, cond2);
        List<QbCondClause> condList = new ArrayList<>();
        condList.add(cond1);
        condList.add(cond2);
        condList.addAll(Arrays.asList(condMore));
        return new RelCondList(rel, condList);
    }

    public static QbCondClause and(List<QbCondClause> condList) {
        return convCondList(Rel.AND, condList);
    }

    public static QbCondClause and(QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        return convCondList(Rel.AND, cond1, cond2, condMore);
    }

    public static QbCondClause or(List<QbCondClause> condList) {
        return convCondList(Rel.OR, condList);
    }

    public static QbCondClause or(QbCondClause cond1, QbCondClause cond2, QbCondClause... condMore) {
        return convCondList(Rel.OR, cond1, cond2, condMore);
    }

    public static QbCondClause not(QbCondClause cond) {
        return new NotCond(cond);
    }

}
