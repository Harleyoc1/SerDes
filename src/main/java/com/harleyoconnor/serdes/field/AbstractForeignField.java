package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesRegistry;
import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.database.Database;

import java.util.function.Function;

/**
 * This class provides a skeletal implementation of the {@link ForeignField} interface, to minimise the
 * effort required to implement it.
 *
 * <p>Its main use is to provide common fields ({@link #foreignField} and {@link #getter}) and to provide
 * simple implementations for {@link ForeignField#getFromValue(Database, Object)} and {@link #get(SerDesable)}.</p>
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the foreign {@code table}'s {@code field}.
 * @param <FKT> The type of the foreign {@code table}.
 * @author Harley O'Connor
 * @see AbstractField
 * @see ForeignField
 */
public abstract class AbstractForeignField<P extends SerDesable<P, ?>, T, FKT extends SerDesable<FKT, ?>> extends AbstractField<P, T> implements ForeignField<P, T, FKT> {

    /** The {@code foreign field} - the {@link Field} that this {@link ForeignField} {@code references}. */
    protected final Field<FKT, T> foreignField;

    /** A {@link Function} getter for {@link FKT}. */
    private final Function<P, FKT> getter;

    @SuppressWarnings("all")
    public AbstractForeignField(String name, Class<P> parentType, Field<FKT, T> foreignField, boolean unique, boolean nullable, Function<P, FKT> getter) {
        super(name, parentType, foreignField.getType(), unique, nullable, null);
        this.foreignField = foreignField;
        this.getter = getter;
    }

    @Override
    public Field<FKT, T> getForeignField() {
        return this.foreignField;
    }

    @Override
    public T get(P object) {
        return this.foreignField.get(this.getActual(object));
    }

    @Override
    public FKT getActual(P object) {
        return this.getter.apply(object);
    }

    @Override
    public FKT getFromValue(Database database, T value) {
        /* We call unsafe here as this should not be called unless there is already a SerDes registered
           for the foreign field, and if not that is a misuse of the API. */
        final var serDes = SerDesRegistry.getUnsafe(this.foreignField.getParentType());

        // Either obtain the object from the currently loaded objects for that SerDes or deserialise it.
        return serDes.getLoadedObjects().stream().filter(object -> this.foreignField.get(object).equals(value)).findFirst().orElseGet(() -> {
            // Selects the result set from the database based on the given value.
            return serDes.deserialise(database, database.selectUnchecked(serDes.getTable(), this.foreignField.getName(), value));
        });
    }

}
