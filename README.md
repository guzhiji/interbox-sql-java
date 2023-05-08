
# A Simple SQL Builder for Java (UNDER EXPERIMENTATION!)

## Simple Select

```java
@Data
@Table("person")
class Person {
    private String id;
    private String name;
    private String groupId;
    private int age;
    private String gender;
}

public class Test {
    public List<Person> listPeople(DataSource ds) {
        return QueryBuilder.selectFrom("person")
                .field("name", "name")
                .orderBy("name", QueryBuilder.Order.ASC)
                .findAll(ds, Person.class);
    }
    public List<Person> listPeople2(DataSource ds) {
        return QueryBuilder.selectFrom(Person.class)
                .field(Person::getName, "name")
                .orderBy(Person::getName, QueryBuilder.Order.ASC)
                .findAll(ds, Person.class);
    }
}
```

## Relatively Complex Select

```java
public class Test {
    public List<Person> listPeople(DataSource ds) {
        return QueryBuilder.selectFrom("person")
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
    }
}
```

## Select with Aggregation

```java
public class Test {
    public int getMaxA(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            return QueryBuilder.selectFrom("test")
                    .field("max(a)", null)
                    .findOne(conn, Integer.TYPE);
        }
    }
}
```

## Update

```java
public class Test {
    public void updateAge(DataSource ds, String name, int age) {
        QueryBuilder.update("person")
                .value("age", age)
                .where(QueryBuilder.eq("name", name))
                .execute(ds);
    }
    public void updateByName(DataSource ds, String name, Person person) {
        QueryBuilder.update(Person.class)
                .values(person)
                .where(QueryBuilder.eq(Person::getName, name))
                .execute(ds);
    }
    public void updateByNameDemo(DataSource ds) {
        Person person = new Person();
        // ...
        person.setAge(10);
        updateByName(ds, "tom", person);
    }
}
```

## Delete

```java
public class Test {
    public void deletePerson(DataSource ds, String id) {
        try (Connection conn = ds.getConnection()) {
            QueryBuilder.deleteFrom("person")
                    .where(QueryBuilder.eq("id", id))
                    .execute(conn);
        }
    }
}
```

