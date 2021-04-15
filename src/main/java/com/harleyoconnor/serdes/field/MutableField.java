package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.database.Database;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@code mutable} ({@code non-final}) implementation of {@link Field}.
 *
 * <p>Provides an implementation of {@link Field#set(Database, SerDesable, Object)} which calls the
 * given {@link BiConsumer} with the relevant parameters.</p>
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the {@code field}.
 * @author Harley O'Connor
 * @see Field
 * @see ImmutableField
 */
public class MutableField<P extends SerDesable<P, ?>, T> extends AbstractField<P, T> {

    /** A {@link BiConsumer} setter for {@link T}. */
    private final BiConsumer<P, T> setter;

    public MutableField(String name, Class<P> parentType, Class<T> fieldType, boolean unique, boolean nullable, Function<P, T> getter, BiConsumer<P, T> setter) {
        super(name, parentType, fieldType, unique, nullable, getter);
        this.setter = setter;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Field<P, T> set(Database database, P object, @Nullable T newValue) {
        this.setter.accept(object, newValue);
        return this;
    }

}
