package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.database.DefaultDatabase;
import com.harleyoconnor.serdes.field.*;
import com.harleyoconnor.serdes.util.CommonCollectors;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

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
 * @param <T> The type for which this instance will handle serialisation and
 *            deserialisation.
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
     * <p>Note that this is a read-only view of the {@link Field}s, and will
     * generally be created as an unmodifiable {@link Set} (and hence modifying
     * it will cause an {@link UnsupportedOperationException} to be thrown).</p>
     *
     * @return A {@link Set} of {@link Field} objects for this {@link SerDes}.
     */
    Set<Field<T, ?>> getFields();

    /**
     * Gets all {@link MutableField} objects for {@link T} as a {@link Set}.
     *
     * <p>Note that this is a read-only view of the {@link Field}s, and will
     * generally be created as an unmodifiable {@link Set} (and hence modifying
     * it will cause an {@link UnsupportedOperationException} to be thrown).</p>
     *
     * @return A {@link Set} of {@link MutableField} objects for this {@link SerDes}.
     */
    default Set<Field<T, ?>> getMutableFields() {
        return this.getFields().stream()
                .filter(Field::isMutable)
                .collect(CommonCollectors.toUnmodifiableLinkedSet());
    }

    /**
     * Gets all {@link ImmutableField} objects for {@link T} as a {@link Set}.
     *
     * <p>Note that this is a read-only view of the {@link Field}s, and will
     * generally be created as an unmodifiable {@link Set} (and hence modifying
     * it will cause an {@link UnsupportedOperationException} to be thrown).</p>
     *
     * @return A {@link Set} of {@link ImmutableField} objects for this {@link SerDes}.
     */
    default Set<Field<T, ?>> getImmutableFields() {
        return this.getFields().stream()
                .filter(field -> !field.isMutable())
                .collect(CommonCollectors.toUnmodifiableLinkedSet());
    }

    /**
     * Gets all {@link ForeignField} objects for {@link T} as a {@link Set}.
     *
     * <p>Note that this is a read-only view of the {@link Field}s, and will
     * generally be created as an unmodifiable {@link Set} (and hence modifying
     * it will cause an {@link UnsupportedOperationException} to be thrown).</p>
     *
     * @return A {@link Set} of {@link ForeignField} objects for this {@link SerDes}.
     */
    @SuppressWarnings("unchecked")
    default Set<ForeignField<T, ?, ?>> getForeignFields() {
        return this.getFields().stream()
                .filter(field -> field instanceof ForeignField)
                .map(field -> ((ForeignField<T, ?, ?>) field))
                .collect(CommonCollectors.toUnmodifiableLinkedSet());
    }

    /**
     * Serialises the given {@code object} of type {@link T} to the
     * {@link DefaultDatabase}, writing all {@link Field} objects back.
     *
     * @param object The {@code object} of type {@link T}.
     */
    default void serialise (final T object) {
        this.serialise(DefaultDatabase.get(), object);
    }

    /**
     * Serialises the given {@code object} of type {@link T} to the specified
     * {@link Database}, writing all {@link Field} objects back.
     *
     * @param database The {@link Database} to serialise from.
     * @param object The {@code object} of type {@link T}.
     */
    void serialise (final Database database, final T object);

    /**
     * Gets a {@link ResultSet} for the specified {@code primaryKeyValue} of type
     * {@link PK} from the {@link DefaultDatabase}.
     *
     * @param primaryKeyValue The {@code primary key}'s value.
     * @return The {@link ResultSet} obtained.
     */
    default ResultSet getResultSet(final PK primaryKeyValue) {
        return this.getResultSet(DefaultDatabase.get(), primaryKeyValue);
    }

    /**
     * Gets a {@link ResultSet} for the specified {@code primaryKeyValue} of type
     * {@link PK} from the specified {@link Database}.
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
     * @param deserialisationResultConsumer The {@link Consumer} of type {@link T} to
     *                                      run after deserialisation is finished.
     */
    void whenNextDeserialised (Consumer<T> deserialisationResultConsumer);

    /**
     * Calls {@link #getResultSet(Database, Object)} for the given {@code primaryKeyValue}
     * and gives the {@link ResultSet} obtained to {@link #deserialise(Database, ResultSet)}.
     *
     * <p>If a {@link Database} {@code object} is required (such as for reading
     * {@link ForeignField}s) {@link DefaultDatabase} is used.</p>
     *
     * @param primaryKeyValue The value of the {@code primary key} for the object to
     *                        deserialise.
     * @return The deserialised {@link Object} of type {@link T}.
     * @since 0.0.5
     */
    default T deserialise (final PK primaryKeyValue) {
        return this.deserialise(DefaultDatabase.get(), primaryKeyValue);
    }

    /**
     * Calls {@link #getResultSet(Database, Object)} for the given {@code primaryKeyValue}
     * and gives the {@link ResultSet} obtained to {@link #deserialise(Database, ResultSet)}.
     *
     * @param database The {@link Database} to deserialise from.
     * @param primaryKeyValue The value of the {@code primary key} for the object to
     *                        deserialise.
     * @return The deserialised {@link Object} of type {@link T}.
     */
    default T deserialise (final Database database, final PK primaryKeyValue) {
        return this.getLoadedObjects().stream().filter(serDesable -> Objects.equals(this.getPrimaryField().get(serDesable), primaryKeyValue))
                .findFirst().orElseGet(() -> this.deserialise(database, this.getResultSet(database, primaryKeyValue)));
    }

    /**
     * Returns a deserialised {@code object} of type {@link T}, which will be
     * obtained from the specified {@link ResultSet}.
     *
     * <p>If a {@link Database} {@code object} is required (such as for reading
     * {@link ForeignField}s) {@link DefaultDatabase} is used.</p>
     *
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @return The deserialised {@code object} of type {@link T}.
     * @since 0.0.5
     */
    default T deserialise(final ResultSet resultSet) {
        return this.deserialise(DefaultDatabase.get(), resultSet);
    }

    /**
     * Returns a deserialised {@code object} of type {@link T}, which will be
     * obtained from the specified {@link ResultSet}.
     *
     * @param database The {@link Database} to read from, if required.
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @return The deserialised {@code object} of type {@link T}.
     */
    default T deserialise(final Database database, final ResultSet resultSet) {
        return this.deserialise(database, resultSet, this.currentlyDeserialising());
    }

    /**
     * Returns a deserialised {@code object} of type {@link T}, which will be
     * obtained from the specified {@link ResultSet}.
     *
     * <p>If a {@link Database} {@code object} is required (such as for reading
     * {@link ForeignField}s) {@link DefaultDatabase} is used.</p>
     *
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @param careful {@code true} if {@link ForeignField} objects shouldn't be
     *                            set until after its {@link SerDes} is finished
     *                            deserialising (to avoid infinite loops).
     * @return The deserialised {@code object} of type {@link T}.
     * @since 0.0.5
     */
    default T deserialise (final ResultSet resultSet, final boolean careful) {
        return this.deserialise(DefaultDatabase.get(), resultSet, careful);
    }

    /**
     * Returns a deserialised {@code object} of type {@link T}, which will be
     * obtained from the specified {@link ResultSet}.
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
     * Creates the SQL {@code table} for this {@link SerDes} in {@link DefaultDatabase}.
     *
     * <p>Note that when creating a table this should be used with caution in order to
     * avoid infinite loops whilst creating foreign key tables. This can be done by
     * making sure it's not {@link #currentlyCreatingTable()}.</p>
     *
     * <p>Generally, implementations should create a {@code boolean} to return in
     * {@link #currentlyCreatingTable()} method, setting it to {@code true} at this method's
     * {@code head} and {@code false} at {@code return}.</p>
     *
     * @since 0.0.5
     */
    default void createTable() {
        this.createTable(DefaultDatabase.get());
    }

    /**
     * Creates the SQL {@code table} for this {@link SerDes} in the specified
     * {@link Database}.
     *
     * <p>Note that when creating a table this should be used with caution in order to
     * avoid infinite loops whilst creating foreign key tables. This can be done by
     * making sure it's not {@link #currentlyCreatingTable()}.</p>
     *
     * <p>Generally, implementations should create a {@code boolean} to return in
     * {@link #currentlyCreatingTable()} method, setting it to {@code true} at this method's
     * {@code head} and {@code false} at {@code return}.</p>
     *
     * @param database The {@link Database} to create the {@code table} in.
     */
    void createTable(final Database database);

    /**
     * Returns whether or not this {@link SerDes} is currently creating a
     * {@code table}. This should be checked whilst creating a table before
     * calling {@link #createTable(Database)} in order to avoid an infinite loop
     * whilst creating foreign key tables.
     *
     * @return {@code true} if an SQL {@code table} is currently being created for
     *         this {@link SerDes}; {@code false} otherwise.
     */
    boolean currentlyCreatingTable();

    /**
     * Converts the given {@link SerDesable} of type {@link T} into a {@link String}
     * representation, including all its {@link Field}s in the following format: <pre>
     *     SimpleClassName{fieldName=fieldValue, anotherFieldName=anotherFieldValue}
     * </pre>
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
