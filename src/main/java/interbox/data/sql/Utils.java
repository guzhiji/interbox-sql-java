package interbox.data.sql;

import interbox.data.sql.annotations.Field;
import interbox.data.sql.annotations.Table;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


class Utils {

    public static String toSnakeCase(String s) {
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

    public static String methodNameToFieldName(String method) {
        String field;
        if ((method.startsWith("set") || method.startsWith("get")) && method.length() > 3) {
            field = method.substring(3);
        } else if (method.startsWith("is") && method.length() > 2) {
            field = method.substring(2);
        } else {
            field = method;
        }
        char firstChar = field.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return Character.toLowerCase(firstChar) + field.substring(1);
        }
        return field;
    }

    public static int inferType(Class<?> cls) {
        if (cls == null)
            return 0; // for unknown, not null
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

    static class FieldMeta {
        final String name;
        final int type;
        final java.lang.reflect.Field field;

        FieldMeta(String name, int type, java.lang.reflect.Field field) {
            this.name = name;
            this.type = type;
            this.field = field;
        }
    }

    static Map<String, FieldMeta> collectFieldMeta(Class<?> cls) {
        Map<String, FieldMeta> fields = new HashMap<>();
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
            fields.put(fieldName, new FieldMeta(
                    fieldName, fieldType, field));
        }
        Class<?> scls = cls.getSuperclass();
        if (scls != null && !scls.equals(Object.class)) {
            Map<String, FieldMeta> sf = collectFieldMeta(scls);
            for (Map.Entry<String, FieldMeta> e : sf.entrySet()) {
                if (!fields.containsKey(e.getKey()))
                    fields.put(e.getKey(), e.getValue());
            }
        }
        return fields;
    }

    private final static Map<Class<?>, Map<String, FieldMeta>> fieldMetaCache = new ConcurrentHashMap<>();

    static Map<String, FieldMeta> getFieldMeta(Class<?> cls) {
        Map<String, FieldMeta> m = fieldMetaCache.get(cls);
        if (m == null) {
            m = collectFieldMeta(cls);
            fieldMetaCache.put(cls, m);
        }
        return m;
    }

    public static int getFieldType(Class<?> cls, String tableField) {
        Map<String, FieldMeta> m = getFieldMeta(cls);
        FieldMeta f = m.get(tableField);
        if (f != null)
            return f.type;
        return 0;
    }

    static SerializedLambda methodRefToSerializedLambda(SerializedFunction<?, ?> methodRef) {
        try {
            Method m = methodRef.getClass().getDeclaredMethod("writeReplace");
            boolean a = m.isAccessible();
            m.setAccessible(true);
            try {
                return (SerializedLambda) m.invoke(methodRef);
            } finally {
                m.setAccessible(a);
            }
        } catch (Throwable th) {
            throw new QbException("fail to read method reference", th);
        }
    }

    static Class<?> typeDescriptorToClass(String typeDescriptor) {
        try {
            String className = typeDescriptor
                    .substring(2, typeDescriptor.indexOf(";"))
                    .replace("/", ".");
            return Class.forName(className);
        } catch (Throwable th) {
            throw new QbException("cannot get class for " + typeDescriptor, th);
        }
    }

    public static <E, R> String getTableFieldName(SerializedFunction<E, R> methodRef) {
        SerializedLambda lambda = methodRefToSerializedLambda(methodRef);
        Class<?> clz = typeDescriptorToClass(lambda.getInstantiatedMethodType());
        String objField = methodNameToFieldName(lambda.getImplMethodName());
        for (FieldMeta fm : getFieldMeta(clz).values()) {
            if (fm.field.getName().equals(objField))
                return fm.name;
        }
        throw new QbException("cannot find field " + objField + " in class " + clz);
    }

    public static List<OAssign> objectToAssignments(Object obj) {
        List<OAssign> out = new ArrayList<>();
        Map<String, FieldMeta> m = getFieldMeta(obj.getClass());
        for (FieldMeta f : m.values()) {
            if (f.type == 0)
                continue;
            out.add(new OAssign(f.name, getFieldValue(obj, f.field), f.type));
        }
        return out;
    }

    public static <T> T resultSetToObject(ResultSet rs, Class<T> cls) {
        try {
            T obj = cls.newInstance();
            Map<String, FieldMeta> m = getFieldMeta(cls);
            for (FieldMeta f : m.values()) {
                setFieldValue(obj, f.field, rs.getObject(f.name));
            }
            return obj;
        } catch (InstantiationException e) {
            throw new QbException("fail to create object for the class " + cls.toString(), e);
        } catch (Throwable th) {
            throw new QbException("fail to map values to object of the class " + cls.toString(), th);
        }
    }

    public static <T> T resultSetFirstCol(ResultSet rs, Class<T> type) {
        try {
            return rs.getObject(1, type);
        } catch (SQLException th) {
            throw new QbException("fail to read first column of result set", th);
        }
    }

    public static void setStmtParams(PreparedStatement stmt, GenCtx genCtx) throws SQLException {
        for (int i = 0; i < genCtx.params.size(); i++) {
            Param p = genCtx.params.get(i);
            if (p.type == null)
                stmt.setObject(i + 1, p.value);
            else
                stmt.setObject(i + 1, p.value, p.type);
        }
    }

    public static void setFieldValue(Object obj, java.lang.reflect.Field field, Object value) {
        boolean a = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new QbException("fail to write value into object", e);
        } finally {
            field.setAccessible(a);
        }
    }

    public static Object getFieldValue(Object obj, java.lang.reflect.Field field) {
        boolean a = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new QbException("fail to read value from object", e);
        } finally {
            field.setAccessible(a);
        }
    }

}
