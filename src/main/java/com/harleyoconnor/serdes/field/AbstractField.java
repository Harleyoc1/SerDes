package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.util.DataTypeConversion;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class provides a skeletal implementation of the {@link Field} interface, to minimise the
 * effort required to implement it.
 *
 * <p>Contains commonly used fields like a {@link #name}, {@link #parentType}, {@link #fieldType},
 * and {@link #getter}, as well as implementing the relevant methods for them.</p>
 *
 * <p>Also provides implementations of {@link #equals(Object)} and {@link #hashCode()}.</p>
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the {@code field}.
 *
 * @author Harley O'Connor
 * @see Field
 */
public abstract class AbstractField<P extends SerDesable<P, ?>, T> implements Field<P, T> {

    protected final String name;

    protected final Class<P> parentType;
    protected final Class<T> fieldType;
    protected final boolean unique;
    protected final boolean nullable;

    protected final Function<P, T> getter;

    public AbstractField(String name, Class<P> parentType, Class<T> fieldType, boolean unique, boolean nullable, Function<P, T> getter) {
        this.name = name;
        this.parentType = parentType;
        this.fieldType = fieldType;
        this.unique = unique;
        this.nullable = nullable;
        this.getter = getter;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<P> getParentType() {
        return this.parentType;
    }

    @Override
    public Class<T> getType() {
        return this.fieldType;
    }

    @Override
    public boolean isUnique() {
        return this.unique;
    }

    @Override
    public boolean isNullable() {
        return this.nullable;
    }

    private static final String VAR_CHAR_MAX = "varchar(255)";

    @Override
    public String getSQLDataType() {
        return DataTypeConversion.getFor(this.getType())
                .map(DataTypeConversion::getSQLDeclaration)
                .map(declaration -> this.isUnique() && this.getType() == String.class ? VAR_CHAR_MAX : declaration)
                .orElse(this.isUnique() ? VAR_CHAR_MAX : "text");
    }

    @Override
    public T get(P object) {
        return this.getter.apply(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        final var that = (AbstractField<?, ?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.parentType, that.parentType)
                && Objects.equals(this.fieldType, that.fieldType)
                && Objects.equals(this.unique, that.unique);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.parentType, this.fieldType, this.unique);
    }

}
