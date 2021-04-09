package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.database.Database;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@code mutable} ({@code non-final}) implementation for {@link ForeignField}.
 *
 * <p>Note that this is currently the only implementation of {@link ForeignField}, as immutable
 * foreign fields will require reflection to prevent infinite loops from happening when two
 * objects have foreign keys of each other (and the entries both happen to reference each other).</p>
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the foreign {@code table}'s {@code field}.
 * @param <FKT> The type of the foreign {@code table}.
 * @author Harley O'Connor
 * @see ForeignField
 * @see MutableField
 */
public class MutableForeignField<P extends SerDesable<P, ?>, T, FKT extends SerDesable<FKT, ?>> extends AbstractForeignField<P, T, FKT> {

    /** A {@link BiConsumer} setter for {@link T}. */
    private final BiConsumer<P, FKT> setter;

    public MutableForeignField(String name, Class<P> parentType, Field<FKT, T> foreignField, boolean unique, Function<P, FKT> getter, BiConsumer<P, FKT> setter) {
        super(name, parentType, foreignField, unique, getter);
        this.setter = setter;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Field<P, T> set(Database database, P object, @Nullable T newValue) {
        this.setter.accept(object, newValue == null ? null : this.getFromValue(database, newValue));
        return this;
    }

}
