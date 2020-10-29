package interbox.data.sql;

import java.util.Arrays;
import java.util.List;


class LogicalCondList extends QbCondClause {
    final QueryBuilder.Logical logical;
    final List<QbCondClause> condList;

    LogicalCondList(QueryBuilder.Logical logical, List<QbCondClause> condList) {
        this.logical = logical;
        this.condList = condList;
    }

    LogicalCondList(QueryBuilder.Logical logical, QbCondClause... condList) {
        this.logical = logical;
        this.condList = Arrays.asList(condList);
    }
}
