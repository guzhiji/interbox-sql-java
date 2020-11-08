package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import org.junit.Test;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class ReflectionTest {

    private static class A {
        String helloWorld;

        public void setHelloWorld(String s) {
            helloWorld = s;
        }

        public String getHelloWorld() {
            return helloWorld;
        }
    }

    @Test
    public void testGetFieldWithoutAnnotation() {
        Map<String, Utils.FieldMeta> m = Utils.collectFieldMeta(A.class);
        assertEquals(1, m.size());
        assertTrue(m.containsKey("hello_world"));
        assertEquals(Types.VARCHAR, m.get("hello_world").type);
    }

    private static class B {

        @Field("javascript")
        private String js;

        @Field(value = "python", type = Types.INTEGER)
        private String py;

        public void setJs(String js) {
            this.js = js;
        }

        public String getJs() {
            return js;
        }

        public void setPy(String py) {
            this.py = py;
        }

        public String getPy() {
            return py;
        }
    }

    @Test
    public void testGetFieldWithAnnotation() {
        Map<String, Utils.FieldMeta> m = Utils.collectFieldMeta(B.class);
        assertEquals(2, m.size());
        assertTrue(m.containsKey("javascript"));
        assertEquals(Types.VARCHAR, m.get("javascript").type);
        assertTrue(m.containsKey("python"));
        assertEquals(Types.INTEGER, m.get("python").type);
    }

    private static class C extends B {

        @Field(value = "python")
        public int py;

        private Integer cSharp;

        public void setPython(int py) {
            this.py = py;
        }

        public int getPython() {
            return py;
        }

        public void setCSharp(Integer cSharp) {
            this.cSharp = cSharp;
        }

        public Integer getCSharp() {
            return cSharp;
        }
    }

    @Test
    public void testGetFieldFromInheritedCls() {
        Map<String, Utils.FieldMeta> m = Utils.collectFieldMeta(C.class);
        assertEquals(3, m.size());
        assertTrue(m.containsKey("javascript"));
        assertEquals(Types.VARCHAR, m.get("javascript").type);
        assertTrue(m.containsKey("python"));
        assertEquals(Types.INTEGER, m.get("python").type);
        assertTrue(m.containsKey("c_sharp"));
        assertEquals(Types.INTEGER, m.get("c_sharp").type);
    }

    private OAssign findAssignment(List<OAssign> all, String field) {
        for (OAssign a : all) {
            if (field.equals(a.field))
                return a;
        }
        return null;
    }

    @Test
    public void testObjectToAssignments() {
        C c = new C();
        c.setJs("1");
        c.setPython(2);
        c.setCSharp(3);
        List<OAssign> assignments = Utils.objectToAssignments(c);

        OAssign js = findAssignment(assignments, "javascript");
        assertNotNull(js);
        assertEquals(Types.VARCHAR, js.type);
        assertEquals("1", js.value);

        OAssign py = findAssignment(assignments, "python");
        assertNotNull(py);
        assertEquals(Types.INTEGER, py.type);
        assertEquals(2, py.value);

        OAssign cs = findAssignment(assignments, "c_sharp");
        assertNotNull(cs);
        assertEquals(Types.INTEGER, cs.type);
        assertEquals(3, cs.value);
    }

}
