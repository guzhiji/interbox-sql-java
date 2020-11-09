package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import interbox.data.sql.annotations.Table;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


public class DbTest {

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1", "sa", "sa");
    }

    @BeforeClass
    public static void prepareDb() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    @Before
    public void prepareTable() throws SQLException {
        try (Connection conn = getConn()) {
            conn.createStatement().execute("create table person (" +
                    "first_name varchar(255)," +
                    "last_name varchar(255)," +
                    "gender char(6)," +
                    "age integer," +
                    "birthday date," +
                    "is_member boolean)");
        }
    }

    @After
    public void destroyTable() throws SQLException {
        try (Connection conn = getConn()) {
            conn.createStatement().execute("drop table person");
        }
    }

    @Table("person")
    static class Person {
        private String firstName;
        private String lastName;
        @Field("gender")
        private String sex;
        private int age;
        private Date birthday;
        private boolean isMember;
    }

    private Person getAlanTuring() {
        Person person = new Person();
        person.firstName = "Alan";
        person.lastName = "Turing";
        person.sex = "male";
        person.age = 22;
        Calendar cal = Calendar.getInstance();
        cal.set(1912, Calendar.JUNE, 23, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        person.birthday = cal.getTime();
        person.isMember = true;
        return person;
    }

    private Person getCandidateOne() {
        Person someone = new Person();
        someone.firstName = "one";
        someone.lastName = "candidate";
        someone.sex = "male";
        someone.age = 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        someone.birthday = cal.getTime();
        return someone;
    }

    private void verifyPerson(Person expected, Person actual) {
        assertEquals(expected.firstName, actual.firstName);
        assertEquals(expected.lastName, actual.lastName);
        assertEquals(expected.sex, actual.sex);
        assertEquals(expected.age, actual.age);
        assertEquals(expected.birthday.getTime(), actual.birthday.getTime());
        assertEquals(expected.isMember, actual.isMember);
    }

    private void verifyPerson(Connection conn, Person person) {
        Person stored = QueryBuilder.selectFrom(Person.class)
                .where(QueryBuilder.eq("first_name", person.firstName))
                .findOne(conn, Person.class);
        verifyPerson(person, stored);
    }

    private Person findPerson(List<Person> all, Person person) {
        for (Person p : all) {
            if (p.firstName != null && p.firstName.equals(person.firstName))
                return p;
        }
        return null;
    }

    @Test
    public void testInsert() throws SQLException {
        Person person = getAlanTuring();
        try (Connection conn = getConn()) {
            int r = QueryBuilder.insertInto(Person.class)
                    .values(person)
                    .execute(conn);
            assertEquals(1, r);
        }
        try (Connection conn = getConn()) {
            verifyPerson(conn, person);
        }
    }

    @Test
    public void testUpdate() throws SQLException {
        Person someone = getCandidateOne();
        try (Connection conn = getConn()) {
            int r1 = QueryBuilder.insertInto("person")
                    .values(someone)
                    .execute(conn);
            int r2 = QueryBuilder.insertInto("person")
                    .value("first_name", "two")
                    .value("last_name", "candidate")
                    .value("gender", "female")
                    .value("age", 1)
                    .value("birthday", new Date())
                    .execute(conn);
            assertEquals(1, r1);
            assertEquals(1, r2);
        }
        Person alan = getAlanTuring();
        try (Connection conn = getConn()) {
            int r = QueryBuilder.update(Person.class)
                    .values(alan)
                    .where(QueryBuilder.eq("first_name", "two"))
                    .execute(conn);
            assertEquals(1, r);
        }
        try (Connection conn = getConn()) {
            verifyPerson(conn, someone);
            verifyPerson(conn, alan);
        }
    }

    @Test
    public void testDelete() throws SQLException {
        Person someone = getCandidateOne();
        Person alan = getAlanTuring();
        try (Connection conn = getConn()) {
            int r1 = QueryBuilder.insertInto(Person.class)
                    .values(someone)
                    .execute(conn);
            int r2 = QueryBuilder.insertInto(Person.class)
                    .values(alan)
                    .execute(conn);
            assertEquals(1, r1);
            assertEquals(1, r2);
        }
        try (Connection conn = getConn()) {
            int preCount = QueryBuilder.selectFrom(Person.class)
                    .field("count(*)", "c")
                    .findOne(conn, Integer.class);
            assertEquals(2, preCount);
            int r = QueryBuilder.deleteFrom(Person.class)
                    .where(QueryBuilder.eq("last_name", "candidate"))
                    .execute(conn);
            assertEquals(1, r);
            int postCount = QueryBuilder.selectFrom(Person.class)
                    .field("count(*)", "c")
                    .findOne(conn, Integer.class);
            assertEquals(1, postCount);
        }
        try (Connection conn = getConn()) {
            boolean es = QueryBuilder.selectFrom(Person.class)
                    .where(QueryBuilder.eq("first_name", someone.firstName))
                    .exists(conn);
            boolean ea = QueryBuilder.selectFrom(Person.class)
                    .where(QueryBuilder.eq("first_name", alan.firstName))
                    .exists(conn);
            assertFalse(es);
            assertTrue(ea);
        }
    }

    @Test(expected = QbException.NoResult.class)
    public void testNoResult() throws SQLException {
        try (Connection conn = getConn()) {
            QueryBuilder.selectFrom(Person.class)
                    .findOne(conn, Person.class);
        }
    }

    @Test
    public void testFindAll() throws SQLException {
        Person someone = getCandidateOne();
        Person alan = getAlanTuring();
        try (Connection conn = getConn()) {
            List<Person> all = QueryBuilder.selectFrom(Person.class)
                    .findAll(conn, Person.class);
            assertEquals(0, all.size());
            int r = QueryBuilder.insertInto(Person.class)
                    .values(someone)
                    .execute(conn);
            assertEquals(1, r);
        }
        try (Connection conn = getConn()) {
            List<Person> all = QueryBuilder.selectFrom(Person.class)
                    .findAll(conn, Person.class);
            assertEquals(1, all.size());
            verifyPerson(someone, all.get(0));
            int r = QueryBuilder.insertInto(Person.class)
                    .values(alan)
                    .execute(conn);
            assertEquals(1, r);
        }
        try (Connection conn = getConn()) {
            List<Person> all = QueryBuilder.selectFrom(Person.class)
                    .findAll(conn, Person.class);
            assertEquals(2, all.size());
            Person someoneStored = findPerson(all, someone);
            Person alanStored = findPerson(all, alan);
            assertNotNull(someoneStored);
            assertNotNull(alanStored);
            verifyPerson(someone, someoneStored);
            verifyPerson(alan, alanStored);
        }
    }

}
