package interbox.data.sql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SnakeCaseTest {

    @Test
    public void testAllLowerCase() {
        assertEquals("abcdef", Utils.toSnakeCase("abcdef"));
    }

    @Test
    public void testAllUpperCase() {
        assertEquals("abcdef", Utils.toSnakeCase("ABCDEF"));
    }

    @Test
    public void testTwoWords() {
        assertEquals("hello_world", Utils.toSnakeCase("helloWorld"));
        assertEquals("hello_world", Utils.toSnakeCase("HelloWorld"));
    }

    @Test
    public void testThreeWords() {
        assertEquals("hello_tom_cat", Utils.toSnakeCase("helloTomCat"));
        assertEquals("hello_tom_cat", Utils.toSnakeCase("HelloTomCat"));
    }

    @Test
    public void testSingleUpperCase() {
        assertEquals("read_a", Utils.toSnakeCase("readA"));
    }

    @Test
    public void testDoubleUpperCase() {
        assertEquals("read_ab", Utils.toSnakeCase("readAB"));
        assertEquals("read_ab", Utils.toSnakeCase("readAb"));
    }

    @Test
    public void testTripleUpperCase() {
        assertEquals("read_abc", Utils.toSnakeCase("readABC"));
    }

    @Test
    public void testDoubleUpperCaseInBetween() {
        assertEquals("read_a_bc", Utils.toSnakeCase("readABc"));
    }

    @Test
    public void testTripleUpperCaseInBetween() {
        assertEquals("read_ab_cd", Utils.toSnakeCase("readABCd"));
    }
}
