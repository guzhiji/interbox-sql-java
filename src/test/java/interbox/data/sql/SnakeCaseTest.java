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

    @Test
    public void testNumAfterUpperCase() {
        assertEquals("read_abc2", Utils.toSnakeCase("readAbc2"));
        assertEquals("read_a2bc", Utils.toSnakeCase("readA2bc"));
    }

    @Test
    public void testNumBeforeLowerCase() {
        assertEquals("read2abc", Utils.toSnakeCase("read2abc"));
        assertEquals("2read_abc", Utils.toSnakeCase("2readAbc"));
    }

    @Test
    public void testNumBeforeUpperCase() {
        assertEquals("read2_abc", Utils.toSnakeCase("read2Abc"));
        assertEquals("2_read_abc", Utils.toSnakeCase("2ReadAbc"));
    }

    @Test
    public void testSingleSeparatorInBetween() {
        assertEquals("read_abc", Utils.toSnakeCase("read-abc"));
        assertEquals("read_abc", Utils.toSnakeCase("read_abc"));
        assertEquals("read_abc", Utils.toSnakeCase("read abc"));

        assertEquals("read_abc", Utils.toSnakeCase("read-Abc"));
        assertEquals("read_abc", Utils.toSnakeCase("read_Abc"));
        assertEquals("read_abc", Utils.toSnakeCase("read Abc"));
    }

    @Test
    public void testDoubleSeparatorsInBetween() {
        assertEquals("read__abc", Utils.toSnakeCase("read--abc"));
        assertEquals("read__abc", Utils.toSnakeCase("read__abc"));
        assertEquals("read__abc", Utils.toSnakeCase("read  abc"));

        assertEquals("read__abc", Utils.toSnakeCase("read--Abc"));
        assertEquals("read__abc", Utils.toSnakeCase("read__Abc"));
        assertEquals("read__abc", Utils.toSnakeCase("read  Abc"));
    }

    @Test
    public void testSeparatorAtFirst() {
        assertEquals("_read_abc", Utils.toSnakeCase("-readAbc"));
        assertEquals("__read_abc", Utils.toSnakeCase("--readAbc"));
        assertEquals("_read_abc", Utils.toSnakeCase("_readAbc"));
        assertEquals("__read_abc", Utils.toSnakeCase("__readAbc"));
        assertEquals("_read_abc", Utils.toSnakeCase(" readAbc"));
        assertEquals("__read_abc", Utils.toSnakeCase("  readAbc"));
    }

}
