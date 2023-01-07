package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.*;


public class LambdaTest {

    private static class ProgrammingLanguages {

        @Field("javascript")
        private String js;

        @Field(value = "python", type = Types.INTEGER)
        private String py;
        private String cpp;

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

        public void setCpp(String cpp) {
            this.cpp = cpp;
        }

        public String getCpp() {
            return cpp;
        }
    }

    @Test
    public void testGetTableFieldName() {
        assertEquals("javascript", Utils.getTableFieldName(ProgrammingLanguages::getJs));
        assertEquals("python", Utils.getTableFieldName(ProgrammingLanguages::getPy));
        assertEquals("cpp", Utils.getTableFieldName(ProgrammingLanguages::getCpp));
    }
}
