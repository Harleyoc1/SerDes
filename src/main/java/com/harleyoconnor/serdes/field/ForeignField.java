package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.database.DefaultDatabase;

import javax.annotation.Nullable;

/**
 * Implementation of {@link Field} which provides compatbility with the
 * {@code foreign key} SQL constraint.
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the foreign {@code table}'s {@code field}.
 * @param <FKT> The type of the foreign {@code table}.
 *
 * @author Harley O'Connor
 * @see Field
 */
public interface ForeignField<P extends SerDesable<P, ?>, T, FKT extends SerDesable<FKT, ?>> extends Field<P, T> {

    /**
     * Gets the {@link Field} that this {@link ForeignField} references.
     *
     * @return The {@link Field} referenced by this field.
     */
    Field<FKT, T> getForeignField();

    /**
     * Gets the actual object of the {@link Field}, of type {@link FKT}, from the
     * specified {@link SerDesable} of type {@link P}.
     *
     * @param object The {@code object} of type {@link P} to get the {@code field}
     *               for.
     * @return The actual value of the {@link Field}, of type {@link FKT}.
     */
    @Nullable
    FKT getActual(P object);

    /**
     * Gets an {@link Object} of type {@link FKT} from the {@code value} of type
     * {@link T} given.
     *
     * <p>If a {@link Database} is required, such as for if the {@code object} for
     * the specified value doesn't already exist in memory, {@link DefaultDatabase}
     * is used.</p>
     *
     * @param value The value of the {@link Field} referenced by this
     *              {@link ForeignField}.
     * @return An {@link Object} of type {@link FKT}, obtainined from the given
     *         {@code value}.
     */
    default FKT getFromValue(@Nullable T value) {
        return this.getFromValue(DefaultDatabase.get(), value);
    }

    /**
     * Gets an {@link Object} of type {@link FKT} from the {@code value} of type
     * {@link T} given.
     *
     * @param database The {@link Database} to get from (if necessary).
     * @param value The value of the {@link Field} referenced by this {@link ForeignField}.
     * @return An {@link Object} of type {@link FKT}, obtainined from the given {@code value}.
     */
    FKT getFromValue(Database database, @Nullable T value);

}
