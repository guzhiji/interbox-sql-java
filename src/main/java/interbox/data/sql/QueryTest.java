package interbox.data.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;


public class QueryTest {

    private static class Person {
        private String id;
        private String name;
        private String groupId;
        private int age;
        private String gender;
    }

    static DataSource ds;
    public static void main(String[] args) throws Exception {
        try (Connection conn = ds.getConnection()) {
            int maxA = QueryBuilder.selectFrom("test")
                    .field("max(a)", null)
                    .findOne(conn, Integer.TYPE);
        }

        List<Person> people = QueryBuilder.selectFrom("person")
                .field("name", "name")
                .orderBy("name", QueryBuilder.Order.ASC)
                .findAll(ds, Person.class);

        QueryBuilder.selectFrom("person")
                .innerJoin(
                        QueryBuilder.selectFrom("participation")
                                .field("person_id", "personId")
                                .field("group_id", "groupId")
                                .where(QueryBuilder.eq("type", "lecture")), "p",
                        QueryBuilder.eqExpr("p.person_id", "person.id"))
                .field("p.groupId", "groupId")
                .field("person.id", "id")
                .field("person.name", "name")
                .field("person.age", "age")
                .field("person.gender", "gender")
                .where(QueryBuilder.compare(QueryBuilder.Comp.GTE, "person.age", 18)
                        .and(QueryBuilder.eq("person.gender", "male")))
                .findAll(ds, Person.class);

        try (Connection conn = ds.getConnection()) {
            int r = QueryBuilder.deleteFrom("person")
                    .where(QueryBuilder.eq("id", 1))
                    .execute(conn);
        }

        QueryBuilder.update("person")
                .value("age", 10)
                .where(QueryBuilder.eq("name", "tom"))
                .execute(ds);

    }

}
