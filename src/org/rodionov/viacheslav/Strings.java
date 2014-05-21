package org.rodionov.viacheslav;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Viacheslav Rodionov (bepcyc@gmail.com)
 * 
 *         Class Strings contains toString method which provides JSON-like
 *         serialization for any object.
 */
public final class Strings {

    /** JSON default format. */
    private static final String JSON_FORMAT = "%s{ \"%s\" : \"%s\" }";

    /** JSON null representation. */
    private static final String NULL_REPRESENTATION = "\"<NULL>\"";

    /**
     * A set containing all local package prefixes. Methods only from this
     * packages will be used.
     */
    private static final Set<String> LOCAL_PACKAGE_PREFIXES = new HashSet<String>() {

        /** serialVersionUID. */
        private static final long serialVersionUID = 8025927834463125260L;

        {
            this.add("com.bmw");
            this.add("de.sulzer");
        }
    };

    /** Java primitive types. */
    private static final Set<String> PRIMITIVES = new HashSet<String>() {

        /** serialVersionUID. */
        private static final long serialVersionUID = 8025927834463125260L;

        {
            this.add("java.lang.Integer");
            this.add("java.lang.Long");
            this.add("java.lang.Short");
            this.add("java.lang.Byte");
            this.add("java.lang.Double");
            this.add("java.lang.Float");
            this.add("java.lang.Boolean");
        }
    };

    /**
     * Private constructor as class contains only static methods.
     */
    private Strings() {
    }

    /**
     * Clever toString method which prints Object as JSOn-like structure.
     * 
     * @param obj
     *            target object to serialize
     * @return string representation of an object
     */
    public static String toString(final Object obj) {
        return String.format("{\n%s\n}", toString(obj, " "));
    }

    /**
     * Clever toString method which prints Object as JSOn-like structure.
     * 
     * @param obj
     *            target object to serialize
     * @param indent
     *            the indent
     * @return string representation of an object
     */
    private static String toString(final Object obj, final String indent) {
        final StringBuilder b = new StringBuilder();
        if (obj == null) {
            b.append(NULL_REPRESENTATION);
        } else {
            final Class<? extends Object> objClass = obj.getClass();
            Boolean hasGetters = false;
            for (final Method method : objClass.getMethods()) {
                final String methodName = method.getName();
                if (methodName.startsWith("get") && methodName.length() > 3
                        && isLocalPackage(method.getDeclaringClass().getName())
                        && method.getParameterTypes().length == 0 && !isStatic(method)) {
                    try {
                        final Method getterMethod = objClass.getMethod(methodName);
                        final Object value = getterMethod.invoke(obj);
                        final String stringValue = toString(value, indent + " ");
                        final String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                        b.append(indent);
                        b.append(String.format("\"%s\" : %s, \n", fieldName, stringValue));
                        hasGetters = true;
                    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException
                            | IllegalAccessException | InvocationTargetException e) {
                        // pass, don't print
                    }
                }
            }
            if (!hasGetters) {
                b.append(callToString(obj, indent + " "));
            } else {
                // remove last comma if it's present
                if (b.length() > 8) {
                    b.delete(b.length() - 3, b.length());
                }
            }
        }
        return b.toString();
    }

    /**
     * Checks if method is static.
     * 
     * @param method
     *            the method to check
     * @return true if method is static, false otherwise
     */
    private static boolean isStatic(final Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Collection to string.
     * 
     * @param values
     *            collection
     * @param collectionName
     *            the collection name
     * @param indent
     *            the indent
     * @return serialized collection
     */
    private static String collectionToString(final Collection<?> values, final String collectionName,
            final String indent) {
        final StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(String.format("{ \"%s\" : ", collectionName));
        valueBuilder.append("[ ");
        for (final Object val : values) {
            valueBuilder.append("\n").append(callToString(val, indent + " "));
            valueBuilder.append(", ");
        }
        if (values.size() > 0) {
            valueBuilder.delete(valueBuilder.length() - 2, valueBuilder.length());
        }
        valueBuilder.append("\n").append(indent).append("] }");
        return valueBuilder.toString();
    }

    /**
     * Call right toString method for an object.
     * 
     * @param obj
     *            target object
     * @param indent
     *            the indent
     * @return serializaed object
     */
    private static String callToString(final Object obj, final String indent) {
        final String result;
        if (obj == null) {
            result = NULL_REPRESENTATION;
        } else if (obj instanceof String) {
            result = String.format(JSON_FORMAT, indent, "String", String.valueOf(obj));
        } else if (isPrimitive(obj)) {
            result = String.format(JSON_FORMAT, indent, obj.getClass().getSimpleName(), String.valueOf(obj));
        } else if (obj instanceof Collection<?>) {
            result = indent + collectionToString((Collection<?>) obj, "Collection", indent);
        } else if (obj instanceof Object[]) {
            result = indent + collectionToString(Arrays.asList((Object[]) obj), "Array", indent);
        } else if (obj instanceof Map.Entry<?, ?>) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
            result = String.format("%s{ \"key\" : %s, \"value\": %s}", indent, callToString(entry.getKey(), ""),
                    callToString(entry.getValue(), ""));
        } else if (obj instanceof Map) {
            result = indent + collectionToString(((Map<?, ?>) obj).entrySet(), "Map", indent);
        } else if (obj instanceof Enum) {
            result = String.format(JSON_FORMAT, indent, obj.getClass().getSimpleName(), String.valueOf(obj));
        } else {
            result = String.valueOf(obj);
        }
        return result;
    }

    /**
     * Checks if object is of primitive type.
     * 
     * @param obj
     *            the obj
     * @return the boolean
     */
    private static Boolean isPrimitive(final Object obj) {
        return PRIMITIVES.contains(obj.getClass().getName());
    }

    /**
     * Checks if package is a local package.
     * 
     * @param packageName
     *            the package name
     * @return the boolean
     */
    private static Boolean isLocalPackage(final String packageName) {
        Boolean result = false;
        for (final String prefix : LOCAL_PACKAGE_PREFIXES) {
            if (packageName.startsWith(prefix)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
