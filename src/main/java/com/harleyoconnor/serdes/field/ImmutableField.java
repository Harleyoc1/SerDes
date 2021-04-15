package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;

import java.util.function.Function;

/**
 * An {@code immutable} implementation of {@code field} - its main use is for {@code final} fields.
 * Takes full advantage of the skeletal implementation provided by {@link AbstractField}.
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the {@code field}.
 * @author Harley O'Connor
 * @see Field
 * @see MutableField
 */
public class ImmutableField<P extends SerDesable<P, ?>, T> extends AbstractField<P, T> {

    public ImmutableField(String name, Class<P> parentType, Class<T> fieldType, boolean unique, boolean nullable, Function<P, T> getter) {
        super(name, parentType, fieldType, unique, nullable, getter);
    }

}
