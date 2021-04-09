package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.field.*;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementations will handle serialising and deserialising an {@link Object} of
 * type {@link T} to and from an SQL table.
 *
 * <p>Implementations will typically contain a {@link Set} of {@link Field} objects,
 * which will both represent a {@link java.lang.reflect.Field} in the Java
 * {@link Class} and a {@code column} in the table.</p>
 *
 * <p>This is named after a SerDes, described as a functional block that serialises
 * and deserialises digital data.</p>
 *
 * <p>All {@link SerDes} objects should be registered via
 * {@link SerDesRegistry#register(SerDes)} for {@link ForeignField} functionality.</p>
 *
 * @param <T> The type for which this instance will handle serialisation and deserialisation.
 * @param <PK> The type of the primary field.
 * @author Harley O'Connor
 * @see Field
 * @see ClassSerDes
 */
public interface SerDes<T extends SerDesable<T, PK>, PK> {

    /**
     * Gets the {@link Class} of the {@link T} type with which this {@link SerDes}
     * handles serialisation and deserialisation for.
     *
     * @return The {@link Class} of {@link T}.
     */
    Class<T> getType();

    /**
     * Gets the name of the SQL {@code table} for this {@link SerDes}.
     *
     * @return The name of the SQL {@code table}.
     */
    String getTable();

    /**
     * Gets the {@link PrimaryField} for this {@link SerDes}.
     *
     * @return The {@link PrimaryField} for this {@link SerDes}.
     */
    PrimaryField<T, PK> getPrimaryField();

    /**
     * Gets a {@link Set} of all currently loaded {@link Object}s of type {@link T}.
     *
     * @return A {@link Set} of loaded {@link Object}s of type {@link T}.
     */
    Set<T> getLoadedObjects();

    /**
     * Gets all {@link Field} objects for {@link T} as a {@link Set}.
     *
     * @return A {@link Set} of {@link Field} objects for this {@link SerDes}.
     */
    Set<Field<T, ?>> getFields();

    /**
     * Gets all {@link MutableField} objects for {@link T} as a {@link Set}.
     *
     * @return A {@link Set} of {@link MutableField} objects for this {@link SerDes}.
     */
    default Set<Field<T, ?>> getMutableFields() {
        return this.getFields().stream().filter(Field::isMutable).collect(Collectors.toSet());
    }

    /**
     * Gets all {@link ImmutableField} objects for {@link T} as a {@link Set}.
     *
     * @return A {@link Set} of {@link ImmutableField} objects for this {@link SerDes}.
     */
    default Set<Field<T, ?>> getImmutableFields() {
        return this.getFields().stream().filter(field -> !field.isMutable()).collect(Collectors.toSet());
    }

    /**
     * Gets all {@link ForeignField} objects for {@link T} as a {@link Set}.
     *
     * @return A {@link Set} of {@link ForeignField} objects for this {@link SerDes}.
     */
    @SuppressWarnings("unchecked")
    default Set<ForeignField<T, ?, ?>> getForeignFields() {
        return this.getFields().stream().filter(field -> field instanceof ForeignField).map(field -> ((ForeignField<T, ?, ?>) field)).collect(Collectors.toSet());
    }

    /**
     * Serialises the given {@code object} of type {@link T}, writing the
     * {@link Field} objects back to the database.
     *
     * @param database The {@link Database} to serialise from.
     * @param object The {@code object} of type {@link T}.
     */
    void serialise (final Database database, final T object);

    /**
     * Gets a {@link ResultSet} for the given {@code primaryKeyValue} of type
     * {@link PK}.
     *
     * @param database The {@link Database} to get the {@link ResultSet} from.
     * @param primaryKeyValue The {@code primary key}'s value.
     * @return The {@link ResultSet} obtained.
     */
    ResultSet getResultSet(final Database database, final PK primaryKeyValue);

    /**
     * Checks if this {@link SerDes} is currently deserialising an object.
     *
     * @return {@code true} if this {@link SerDes} is currently deserialising
     *         an {@link Object}; {@code false} otherwise.
     */
    boolean currentlyDeserialising ();

    /**
     * Adds a {@link Consumer} of type {@link T} which will have
     * {@link Consumer#accept(Object)} called once the next {@link Object}
     * has finished deserialising.
     *
     * @param deserialisationResultConsumer The {@link Consumer} of type {@link T} to run after deserialisation is finished.
     */
    void whenNextDeserialised (Consumer<T> deserialisationResultConsumer);

    /**
     * Calls {@link #getResultSet(Database, Object)} for the given {@code primaryKeyValue}
     * and gives the {@link ResultSet} obtained to {@link #deserialise(Database, ResultSet)}.
     *
     * @param database The {@link Database} to deserialise from.
     * @param primaryKeyValue The value of the {@code primary key} for the object to deserialise.
     * @return The deserialised {@link Object} of type {@link T}.
     */
    default T deserialise (final Database database, final PK primaryKeyValue) {
        return this.getLoadedObjects().stream().filter(serDesable -> this.getPrimaryField().get(serDesable).equals(primaryKeyValue))
                .findFirst().orElseGet(() -> this.deserialise(database, this.getResultSet(database, primaryKeyValue)));
    }

    default T deserialise(final Database database, final ResultSet resultSet) {
        return this.deserialise(database, resultSet, this.currentlyDeserialising());
    }

    /**
     * Returns a deserialised {@code object} of type {@link T}, which will be
     * obtained from the given {@link ResultSet}.
     *
     * @param database The {@link Database} to read from, if required.
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @param careful {@code true} if {@link ForeignField} objects shouldn't be
     *                            set until after its {@link SerDes} is finished
     *                            deserialising (to avoid infinite loops).
     * @return The deserialised {@code object} of type {@link T}.
     */
    T deserialise (final Database database, final ResultSet resultSet, final boolean careful);

    /**
     * A careful version of {@link #deserialise(Database, ResultSet)} which doesn't set
     * {@link ForeignField} objects until after their {@link SerDes} is finished
     * deserialising.
     *
     * <p>Note that this method will not check if the {@link ForeignField} object's
     * {@link SerDes} is currently deserialising, that should be checked through its
     * {@link #currentlyDeserialising()} method before calling this.</p>
     *
     * @param database The {@link Database} to read from, if required.
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @return The deserialised {@code object} of type {@link T}.
     */
    T deserialiseCareful(final Database database, final ResultSet resultSet);

    /**
     * Converts the given {@link SerDesable} of type {@link T} into a {@link String}
     * representation, including all its {@link Field}s in the following format:
     * <br>
     *
     * <pre>SimpleClassName{fieldName=fieldValue, anotherFieldName=anotherFieldValue}</pre>
     *
     * @param serDesable The {@link Object} of type {@link T} to convert to a {@link String}.
     * @return The formatted {@link String} representation.
     */
    default String toString(final T serDesable) {
        final var stringBuilder = new StringBuilder(this.getType().getSimpleName()).append("{");

        final var fields = this.getFields();
        final var fieldIterator = fields.iterator();

        for (int i = 0; i < fields.size(); i++) {
            final var field = fieldIterator.next();
            stringBuilder.append(field.getName()).append("=").append(field.get(serDesable)).append(i == fields.size() - 1 ? "" : ", ");
        }

        return stringBuilder.append("}").toString();
    }

}
