package com.harleyoconnor.serdes.util;

import com.harleyoconnor.javautilities.util.Primitive;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds Java data types that can be converted to an SQL data type,
 * providing their declaration.
 *
 * @author Harley O'Connor
 * @since 0.0.3
 */
public enum DataTypeConversion {
    BOOLEAN(boolean.class, "bool"),
    BYTE(byte.class, "tinyint"),
    SHORT(short.class, "smallint"),
    INT(int.class, "int"),
    LONG(long.class, "bigint"),
    FLOAT(float.class, "float"),
    DOUBLE(double.class, "double"),
    CHAR(char.class, "char", 1),
    STRING(String.class, "text"),
    DATE(Date.class, "datetime");

    /** The Java {@link Class}. Should be the primitive version if applicable. */
    private final Class<?> javaClass;
    /** The name of the SQL data type. */
    private final String name;
    /** The {@code arguments}, if applicable. */
    private final String args;

    DataTypeConversion(final Class<?> javaClass, final String name, final Object... args) {
        this.javaClass = javaClass;
        this.name = name;

        if (args.length > 0)
            this.args = "(" + Stream.of(args).map(String::valueOf).collect(Collectors.joining(",")) + ")";
        else this.args = "";
    }

    /**
     * Gets the {@link #javaClass} for this {@link DataTypeConversion} object.
     *
     * @return The {@link #javaClass} for this {@link DataTypeConversion} object.
     */
    public Class<?> getJavaClass() {
        return this.javaClass;
    }

    /**
     * Gets the SQL declaration for this data type. This is in the format:
     * {@code name(arg1, arg2)}, where the {@code name} is {@link #name}
     * and the args are {@link #args} (formatted in
     * {@link #DataTypeConversion(Class, String, Object...)}).
     *
     * @return The SQL declaration for the data type.
     */
    public String getSQLDeclaration() {
        return this.name + this.args;
    }

    /**
     * Gets an {@link Optional} for the {@link DataTypeConversion} for the
     * given {@code javaClass}.
     *
     * @param javaClass The {@link Class} of the type to get the
     *                  {@link DataTypeConversion} for.
     * @return An {@link Optional}, which either contains the
     *         {@link DataTypeConversion} object or is empty.
     */
    public static Optional<DataTypeConversion> getFor(final Class<?> javaClass) {
        // Convert the class to its primitive type if one exists.
        final Class<?> finalClass = Primitive.fromOrSelf(javaClass);

        // Filter through all conversions and try to find the first that matches the given class.
        return Stream.of(DataTypeConversion.values()).filter(dataTypeConversion ->
                dataTypeConversion.getJavaClass() == finalClass).findFirst();
    }

}
