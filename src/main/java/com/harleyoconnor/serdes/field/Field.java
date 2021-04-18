package com.harleyoconnor.serdes.field;

import com.harleyoconnor.serdes.SerDes;
import com.harleyoconnor.serdes.SerDesRegistry;
import com.harleyoconnor.serdes.SerDesable;
import com.harleyoconnor.serdes.database.Database;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Represents a {@code field}, both in the sense of a Java {@link Class} field and an SQL
 * {@code field}.
 *
 * @param <P> The type of the parent {@link Class}.
 * @param <T> The type of the {@code field}.
 *
 * @author Harley O'Connor
 * @see ForeignField
 * @see MutableField
 * @see ImmutableField
 */
public interface Field<P extends SerDesable<P, ?>, T> {

    /**
     * Gets the {@code name} of the {@code field}.
     *
     * <p>Note that this should get the name of the SQL {@code field}, and that it may
     * not necessarily be the same as the Java {@code field} name due to differences in
     * their naming conventions.</p>
     *
     * @return The {@code name} of the {@code field}.
     */
    String getName();

    /**
     * Gets the {@link Class} of type {@link P}, the {@link SerDesable} which holds
     * this {@link Field}.
     *
     * @return The {@link Class} of type {@link P}.
     */
    Class<P> getParentType();

    /**
     * Gets the {@link SerDes} for the parent {@link SerDesable} to this {@link Field}.
     *
     * @return An {@link Optional} for the parent {@link SerDes}.
     */
    default Optional<SerDes<P, ?>> getParentSerDes () {
        return SerDesRegistry.get(this.getParentType());
    }

    /**
     * Gets the {@link Class} of type {@link T}, the type for the {@code field}.
     *
     * @return The {@link Class} of type {@link T} of the {@code field}.
     */
    Class<T> getType();

    /**
     * Asserts if the SQL {@code field} is {@code unique}.
     *
     * @return {@code true} if the SQL {@code field} is unique; {@code false} otherwise.
     */
    boolean isUnique();

    /**
     * Asserts if this {@link Field} object is {@code mutable}. If this returns
     * {@code true}, calling {@link #set(Database, SerDesable, Object)} should
     * <b>not</b> produce an {@link UnsupportedOperationException}.
     *
     * <p>This allows {@link Field} implementations to declare themselves as mutable
     * without having to extend {@link MutableField}, and should <b>always</b> be
     * used over an {@code instanceof} check!</p>
     *
     * @return {@code true} if this field is {@code non-final} (can be mutated);
     *         {@code false} otherwise.
     */
    default boolean isMutable() {
        return false;
    }

    /**
     * Asserts if this {@link Field} is {@link Nullable}. Note that if this
     * returns {@code false}, the default SQL declaration will use {@code not
     * null}.
     *
     * @return {@code true} if this field is {@link Nullable}; {@code false}
     * if not.
     */
    boolean isNullable();

    /**
     * Gets the value of the {@code field} of type {@link T} in the given {@code object}
     * of type {@link P}.
     *
     * @param object The {@code object} of type {@link P} to get the {@code field} for.
     * @return The value of type {@link T} of the {@code field}.
     */
    T get(P object);

    /**
     * Returns the SQL declaration for this {@link Field}, used to construct it when
     * creating or editing the {@code table}.
     *
     * @return The SQL declaration for this {@link Field}.
     */
    default String getSQLDeclaration() {
        return this.getName() + " " + this.getSQLDataType() + (this.isUnique() ? " unique " : " ") +
                (this.isNullable() ? "" : "not null");
    }

    /**
     * Returns the SQL data type for this {@link Field} as a {@link String}.
     *
     * @return The SQL data type for this {@link Field}.
     */
    String getSQLDataType();

    /**
     * Sets the given {@code newValue} of type {@link T} to this {@link Field} in the given
     * {@code object} of type {@link P}.
     *
     * @param database The {@link Database} to read from, if required.
     * @param object The {@code object} of type {@link P} to set the {@code field} for.
     * @param newValue The {@code newValue} of type {@link T} to set.
     * @return This {@link Field} for chaining.
     * @throws UnsupportedOperationException If this implementation of is not {@code mutable}.
     *                                       Mutability should be checked via
     *                                       {@link #isMutable()}.
     */
    default Field<P, T> set (Database database, P object, @Nullable T newValue) {
        throw new UnsupportedOperationException("Cannot set Field for Field implementation '" + this.getClass().getSimpleName() + "'.");
    }

}
