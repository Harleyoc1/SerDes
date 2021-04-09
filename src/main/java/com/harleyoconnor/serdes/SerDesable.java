package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.field.PrimaryField;

/**
 * An interface used to indicate that a class declaration has a {@link SerDes}
 * associated for serialising and deserialising it. Classes will need to implement
 * this to create a {@link SerDes}, and should return it via an implementation of
 * {@link #getSerDes()}.
 *
 * <p>The {@link SerDes} instance will usually be stored in the relevant class as a
 * {@code final}, {@code static} field with {@code public} access.</p>
 *
 * @param <T> The extending {@link Class} type of this.
 * @param <PK> The {@code primary key} type.
 * @author Harley O'Connor
 * @see SerDes
 * @see AbstractSerDesable
 */
public interface SerDesable<T extends SerDesable<T, PK>, PK> {

    /**
     * Gets the {@link SerDes} object for this {@link SerDesable}.
     *
     * <p>It is recommended for this to be stored as a {@code static}
     * field of the relevant subclass.</p>
     *
     * @return The {@link SerDes} object.
     */
    SerDes<T, PK> getSerDes ();

    /**
     * Gets the {@link PrimaryField} with primary key type {@link PK}
     * for this {@link SerDesable}.
     *
     * <p>It is recommended for this to be stored as a {@code static}
     * field of the relevant subclass.</p>
     *
     * @return The {@link PrimaryField} for this {@link SerDesable}.
     */
    PrimaryField<T, PK> getPrimaryField();

    /**
     * Serialises this {@link SerDesable} using
     * {@link SerDes#serialise(Database, SerDesable)}.
     *
     * @param database The {@link Database} to serialise from.
     */
    @SuppressWarnings("unchecked")
    default void serialise(final Database database) {
        this.getSerDes().serialise(database, (T) this);
    }

}
