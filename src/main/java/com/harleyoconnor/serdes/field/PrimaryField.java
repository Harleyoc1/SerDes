package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;

import java.util.function.Function;

/**
 * An extension of {@link ImmutableField} that forces {@link #unique} to be true (as primary
 * keys should always be unique).
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the {@code field}.
 * @author Harley O'Connor
 * @see ImmutableField
 */
public class PrimaryField<P extends SerDesable<P, ?>, T> extends ImmutableField<P, T> {

    public PrimaryField(String name, Class<P> parentType, Class<T> fieldType, Function<P, T> getter) {
        super(name, parentType, fieldType, true, getter);
    }

}
