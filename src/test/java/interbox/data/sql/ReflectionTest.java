package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import org.junit.Test;

import java.sql.Types;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ReflectionTest {

    private static class A {
        String helloWorld;
    }

    @Test
    public void testGetFieldWithoutAnnotation() {
        Map<String, Integer> m = Utils.getFieldMeta(A.class);
        assertEquals(1, m.size());
        assertTrue(m.containsKey("hello_world"));
        assertEquals(Types.VARCHAR, m.get("hello_world").longValue());
    }

    private static class B {

        @Field("javascript")
        private String js;

        @Field(value = "python", type = Types.INTEGER)
        private String py;

    }

    @Test
    public void testGetFieldWithAnnotation() {
        Map<String, Integer> m = Utils.getFieldMeta(B.class);
        assertEquals(2, m.size());
        assertTrue(m.containsKey("javascript"));
        assertEquals(Types.VARCHAR, m.get("javascript").longValue());
        assertTrue(m.containsKey("python"));
        assertEquals(Types.INTEGER, m.get("python").longValue());
    }

    private static class C extends B {

        @Field(value = "python")
        private int py;

        private Integer cSharp;
    }

    @Test
    public void testGetFieldFromInheritedCls() {
        Map<String, Integer> m = Utils.getFieldMeta(C.class);
        assertEquals(3, m.size());
        assertTrue(m.containsKey("javascript"));
        assertEquals(Types.VARCHAR, m.get("javascript").longValue());
        assertTrue(m.containsKey("python"));
        assertEquals(Types.INTEGER, m.get("python").longValue());
        assertTrue(m.containsKey("c_sharp"));
        assertEquals(Types.INTEGER, m.get("c_sharp").longValue());
    }

}
