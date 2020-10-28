package interbox.data.sql;

import java.util.Arrays;
import java.util.List;


class RelCondList extends QbCondClause {
    final QueryBuilder.Rel rel;
    final List<QbCondClause> condList;

    RelCondList(QueryBuilder.Rel rel, List<QbCondClause> condList) {
        this.rel = rel;
        this.condList = condList;
    }

    RelCondList(QueryBuilder.Rel rel, QbCondClause... condList) {
        this.rel = rel;
        this.condList = Arrays.asList(condList);
    }
}
