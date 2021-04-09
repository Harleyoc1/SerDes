package com.harleyoconnor.serdes.util;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class PrimitiveClass {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_CLASSES = new MapContainer.HashMapContainer<Class<?>, Class<?>>()
            .put(Boolean.class, Boolean.TYPE)
            .put(Byte.class, Byte.TYPE)
            .put(Short.class, Short.TYPE)
            .put(Integer.class, Integer.TYPE)
            .put(Long.class, Long.TYPE)
            .put(Float.class, Float.TYPE)
            .put(Double.class, Double.TYPE)
            .put(Character.class, Character.TYPE).get();

    /**
     * Checks if the given {@link Class} is convertible to a primitive class by
     * {@link #convert(Class)}.
     *
     * @param clazz The {@link Class} to check.
     * @return {@code true} if it can be converted; {@code false} otherwise.
     */
    public static boolean convertible(final Class<?> clazz) {
        return PRIMITIVE_CLASSES.containsKey(clazz);
    }

    /**
     * Converts the given {@link Class} to its primitive {@link Class} and returns it, or
     * returns the given {@link Class} if it did not have a primitive type.
     *
     * @param clazz The {@link Class} to convert.
     * @return The primitive {@link Class} if a converter is registered; otherwise the
     *         given {@link Class}.
     */
    public static Class<?> convert(final Class<?> clazz) {
        return PRIMITIVE_CLASSES.getOrDefault(clazz, clazz);
    }

}
