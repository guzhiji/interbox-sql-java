package interbox.data.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class QbCondClause {
    private boolean enabled = true;
    boolean negated = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
    }

    public QbCondClause and(List<QbCondClause> condList) {
        List<QbCondClause> newCondList = new ArrayList<>();
        newCondList.add(this);
        newCondList.addAll(condList);
        return QueryBuilder.and(newCondList);
    }

    public QbCondClause and(QbCondClause... condList) {
        return and(Arrays.asList(condList));
    }

    public QbCondClause or(List<QbCondClause> condList) {
        List<QbCondClause> newCondList = new ArrayList<>();
        newCondList.add(this);
        newCondList.addAll(condList);
        return QueryBuilder.or(newCondList);
    }

    public QbCondClause or(QbCondClause... condList) {
        return or(Arrays.asList(condList));
    }

    public QbCondClause not() {
        return new NotCond(this);
    }

}
