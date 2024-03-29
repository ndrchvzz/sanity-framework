package func.basic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a sane implementation of equals(), hashCode() and toString() for
 * immutable classes.
 */
public abstract class Ob {
    private static final Map<Class,List<Field>> clToFields = new ConcurrentHashMap<>();

    private static List<Field> getFields(Class cl) {
        if (clToFields.containsKey(cl)) {
            return clToFields.get(cl);
        } else {
            List<Field> fields = new ArrayList<>();
            Field[] fields1 = cl.getDeclaredFields();
            for (Field field : fields1) {
                int modifiers = field.getModifiers();
                if (!field.isSynthetic() && !Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            clToFields.put(cl, fields);
            return fields;
        }
    }

    @Override
    public int hashCode() {
        Class<?> cl = getClass();
        int hash = 1;
        while (cl != Ob.class) {
            hash = 31 * hash + getHash(cl);
            cl = cl.getSuperclass();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> cl = getClass();
        if (!(obj.getClass().isInstance(this))) {
            return false;
        }

        while (cl != Ob.class) {
            if (!(cl.isInstance(obj)) || !isEqual(cl, obj)) {
                return false;
            }
            cl = cl.getSuperclass();
        }
        return true;
    }

    @Override
    public String toString() {
        Class<?> cl = getClass();
        StringBuilder sb = new StringBuilder();

        if (cl != Ob.class) {
            sb.insert(0, fieldsToString(cl));
            cl = cl.getSuperclass();
        }

        while (cl != Ob.class) {
            sb.insert(0, fieldsToString(cl) + ", ");
            cl = cl.getSuperclass();
        }
        return getClass().getSimpleName() + "[" + sb.append("]").toString();
    }

    private int getHash(Class cl) {
        try {
            int hash = 1;
            for (Field field : getFields(cl)) {
                Object o = field.get(this);
                int newHash;
                if (o == null) {
                    newHash = 0;
                } else if (o instanceof BigDecimal) {
                    // BigDecimal with different scales have different hash code even
                    // if they represent the same value.
                    newHash = new Double(((BigDecimal) o).doubleValue()).hashCode();
                } else {
                    newHash = o.hashCode();
                }
                hash = 31 * hash + newHash;
            }
            return hash;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEqual(Class cl, Object obj) {
        for (Field field : getFields(cl)) {
            try {
                Object o = field.get(this);
                Object that = field.get(obj);
                if (o == null && that != null) {
                    return false;
                }
                // BigDecimal with different scales have different hash code even
                // if they represent the same value.
                if (o instanceof BigDecimal && that instanceof BigDecimal) {
                    if (((BigDecimal) o).compareTo((BigDecimal) that) != 0) {
                        return false;
                    }
                } else if (o != null && !o.equals(that)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public static String formatObject(Object obj) {
        if (obj instanceof String) {
            return "\"" + ((String) obj).replace("\"", "\\\"") + "\"";
        } else if (obj instanceof Character) {
            return "'" + obj + "'";
        } else if (obj instanceof Collection) {
            Collection coll = (Collection) obj;
            if (coll.isEmpty()) {
                return "[]";
            } else {
                StringBuilder sb = new StringBuilder("[");
                Iterator iterator = coll.iterator();
                sb.append(Ob.formatObject(iterator.next()));
                while (iterator.hasNext()) {
                    sb.append(", ").append(Ob.formatObject(iterator.next()));
                }
                return sb.append(']').toString();
            }
        } else {
            return String.valueOf(obj);
        }
    }

    private String fieldsToString(Class cl) {
        try {
            StringBuilder sb = new StringBuilder();
            List<Field> fields = getFields(cl);
            if (!fields.isEmpty()) {
                Field first = fields.get(0);
                sb.append(first.getName()).append("=").append(formatObject(first.get(this)));
            }
            for (int i = 1, fieldsSize = fields.size(); i < fieldsSize; i++) {
                Field field = fields.get(i);
                sb.append(", ").append(field.getName()).append("=").
                    append(formatObject(field.get(this)));
            }
            return sb.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
