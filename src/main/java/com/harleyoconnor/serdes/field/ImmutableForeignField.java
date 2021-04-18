package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;

import java.util.function.Function;

/**
 * An {@code immutable} ({@code final}) implementation for {@link ForeignField}.
 *
 * <p>This should be used with caution, as having two tables with foreign keys referencing
 * each other may cause an infinite loop whilst loading them from the database.</p>
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the foreign {@code table}'s {@code field}.
 * @param <FKT> The type of the foreign {@code table}.
 *
 * @author Harley O'Connor
 * @see ForeignField
 * @see MutableForeignField
 * @see ImmutableField
 * @since SerDes 0.0.4
 */
public final class ImmutableForeignField<P extends SerDesable<P, ?>, T, FKT extends SerDesable<FKT, ?>> extends AbstractForeignField<P, T, FKT> {

    public ImmutableForeignField(String name, Class<P> parentType, Field<FKT, T> foreignField, boolean unique, boolean nullable, Function<P, FKT> getter) {
        super(name, parentType, foreignField, unique, nullable, getter);
    }

}
