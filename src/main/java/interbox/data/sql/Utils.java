package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import interbox.data.sql.annotations.Table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


class Utils {

    static String toSnakeCase(String s) {
        if (s == null)
            return null;
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Character pc = null;
        for (char c : s.toCharArray()) {
            switch (c) {
                case ' ':
                case '-':
                case '_':
                    if (pc != null) {
                        sb.append(pc);
                        pc = null;
                    }
                    tokens.add(sb.toString());
                    if (sb.length() > 0)
                        sb = new StringBuilder();
                    break;
                default:
                    if (pc != null) {
                        if (Character.isUpperCase(pc)) {
                            if (!Character.isUpperCase(c)) {
                                // ABc,aBc: upper(B)->lower(c): a,bc
                                if (sb.length() > 0) {
                                    tokens.add(sb.toString());
                                    sb = new StringBuilder();
                                }
                            }
                            sb.append(Character.toLowerCase(pc));
                        } else if (Character.isUpperCase(c)) {
                            // aB: lower(a)->upper(B): a,b
                            sb.append(pc);
                            tokens.add(sb.toString());
                            sb = new StringBuilder();
                        } else {
                            sb.append(pc);
                        }
                    }
                    pc = c;
                    break;
            }
        }
        if (pc != null) {
            sb.append(Character.toLowerCase(pc));
            tokens.add(sb.toString());
        }
        return String.join("_", tokens);
    }

    static int inferType(Class<?> cls) {
        if (cls == null)
            return 0;
        if (Short.TYPE == cls || Short.class == cls)
            return Types.INTEGER;
        if (Integer.TYPE == cls || Integer.class == cls)
            return Types.INTEGER;
        if (Long.TYPE == cls || Long.class == cls)
            return Types.INTEGER;
        if (BigInteger.class.isAssignableFrom(cls))
            return Types.BIGINT;
        if (Float.TYPE == cls || Float.class == cls)
            return Types.FLOAT;
        if (Double.TYPE == cls ||
                Double.class == cls ||
                BigDecimal.class.isAssignableFrom(cls))
            return Types.DOUBLE;
        if (Boolean.TYPE == cls || Boolean.class == cls)
            return Types.BOOLEAN;
        if (Character.TYPE == cls || Character.class == cls)
            return Types.CHAR;
        if (String.class == cls || cls.isEnum())
            return Types.VARCHAR;
        if (Time.class.isAssignableFrom(cls))
            return Types.TIME;
        if (Date.class.isAssignableFrom(cls) ||
                LocalDate.class == cls)
            return Types.DATE;
        if (Timestamp.class.isAssignableFrom(cls) ||
                java.util.Date.class.isAssignableFrom(cls) ||
                Calendar.class.isAssignableFrom(cls) ||
                LocalDateTime.class == cls)
            return Types.TIMESTAMP;
        return 0;
    }

    public static String getTableName(Class<?> cls) {
        Table an = cls.getAnnotation(Table.class);
        if (an == null)
            return null;
        return an.value();
    }

    private final static Map<String, Map<String, Integer>> classMetaCache = new ConcurrentHashMap<>();

    static Map<String, Integer> getFieldMeta(Class<?> cls) {
        Map<String, Integer> fields = new HashMap<>();
        for (java.lang.reflect.Field field : cls.getDeclaredFields()) {
            Field an = field.getAnnotation(Field.class);
            String fieldName;
            int fieldType;
            if (an == null) {
                fieldName = toSnakeCase(field.getName());
                fieldType = inferType(field.getType());
            } else if (an.type() == 0) {
                fieldName = an.value();
                fieldType = inferType(field.getType());
            } else {
                fieldName = an.value();
                fieldType = an.type();
            }
            fields.put(fieldName, fieldType);
        }
        Class<?> scls = cls.getSuperclass();
        if (scls != null && !scls.equals(Object.class)) {
            Map<String, Integer> sf = getFieldMeta(scls);
            for (Map.Entry<String, Integer> e : sf.entrySet()) {
                if (!fields.containsKey(e.getKey()))
                    fields.put(e.getKey(), e.getValue());
            }
        }
        return fields;
    }

}
