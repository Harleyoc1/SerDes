package com.harleyoconnor.serdes;

import com.harleyoconnor.javautilities.collection.WeakHashSet;
import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.exception.NoSuchConstructorException;
import com.harleyoconnor.serdes.field.*;
import com.harleyoconnor.serdes.util.CommonCollectors;
import com.harleyoconnor.serdes.util.Null;
import com.harleyoconnor.serdes.util.PrimitiveClass;
import com.harleyoconnor.serdes.util.ResultSetConversions;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides a skeletal implementation of the {@link SerDes} interface,
 * to minimise the effort required to implement it.
 *
 * @param <T> The type for which this instance will handle serialisation and
 *            deserialisation.
 * @param <PK> The type of the primary field.
 *
 * @author Harley O'Connor
 * @see SerDes
 * @see ClassSerDes
 */
public abstract class AbstractSerDes<T extends SerDesable<T, PK>, PK> implements SerDes<T, PK> {

    protected final Class<T> type;
    protected final String table;
    protected final PrimaryField<T, PK> primaryField;

    protected final LinkedHashSet<Field<T, ?>> immutableFields;

    protected final Set<T> loadedObjects = new WeakHashSet<>();

    protected final List<Consumer<T>> nextDeserialisedResultConsumers = new ArrayList<>();
    private boolean currentlyDeserialising;

    public AbstractSerDes(Class<T> type, String table, PrimaryField<T, PK> primaryField, LinkedHashSet<Field<T, ?>> immutableFields) {
        this.type = type;
        this.table = table;
        this.primaryField = primaryField;
        this.immutableFields = immutableFields;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link Class} of {@link T}.
     */
    @Override
    public Class<T> getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     *
     * @return The name of the SQL {@code table}.
     */
    @Override
    public String getTable() {
        return this.table;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link PrimaryField} for this {@link SerDes}.
     */
    @Override
    public PrimaryField<T, PK> getPrimaryField() {
        return this.primaryField;
    }

    /**
     * {@inheritDoc}
     *
     * @return A {@link Set} of loaded {@link Object}s of type {@link T}.
     */
    @Override
    public Set<T> getLoadedObjects() {
        return this.loadedObjects;
    }

    /**
     * {@inheritDoc}
     *
     * @return A {@link Set} of {@link ImmutableField} objects for this
     *         {@link SerDes}.
     */
    @Override
    public Set<Field<T, ?>> getImmutableFields() {
        return this.immutableFields.stream()
                .collect(CommonCollectors.toUnmodifiableLinkedSet());
    }

    /**
     * {@inheritDoc}
     *
     * @param database The {@link Database} to serialise from.
     * @param object The {@code object} of type {@link T}.
     */
    @Override
    public void serialise(Database database, T object) {
        // If it doesn't already exist, insert the new value.
        if (!database.valueExists(this.table, this.primaryField.getName(),
                this.primaryField.get(object))) {
            database.insertUnchecked(this.table,
                    this.toInsertableMap(object, this.getFields()));
            return;
        }

        // Otherwise, update the value.
        database.updateUnchecked(this.table, this.primaryField.getName(),
                this.primaryField.get(object),
                this.toInsertableMap(object, this.getMutableFields().stream()
                        .map(field -> ((Field<T, ?>) field))
                        .collect(Collectors.toUnmodifiableSet())));
    }

    /**
     * Creates an "insertable" {@link LinkedHashMap} for the given {@code fields}.
     * This refers to the ability to pass it to
     * {@link Database#update(String, String, Object, LinkedHashMap)}.
     *
     * @param object The {@code object} of type {@link T} being serialised.
     * @param fields The {@link Set} of {@link Field}s being inserted.
     * @return The {@link LinkedHashMap} of {@link Field}s and their equivalent
     *         values in the given {@code object} of type {@link T}.
     */
    private LinkedHashMap<String, Object> toInsertableMap(final T object,
                                                          final Set<Field<T, ?>> fields) {
        return fields.stream()
                .filter(field -> field.get(object) != null)
                .collect(Collectors.toMap(Field::getName,
                        field -> Objects.requireNonNull(field.get(object)),
                        (m1, m2) -> m2, LinkedHashMap::new));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if this {@link SerDes} is currently deserialising an
     *         {@link Object}; {@code false} otherwise.
     */
    @Override
    public boolean currentlyDeserialising() {
        return this.currentlyDeserialising;
    }

    /**
     * {@inheritDoc}
     *
     * @param deserialisationResultConsumer The {@link Consumer} of type {@link T} to
     */
    @Override
    public void whenNextDeserialised(Consumer<T> deserialisationResultConsumer) {
        this.nextDeserialisedResultConsumers.add(deserialisationResultConsumer);
    }

    /**
     * {@inheritDoc}
     *
     * @param database The {@link Database} to get the {@link ResultSet} from.
     * @param primaryKeyValue The {@code primary key}'s value.
     * @return The {@link ResultSet} obtained.
     */
    @Override
    public ResultSet getResultSet(Database database, PK primaryKeyValue) {
        return database.selectUnchecked(this.table, this.primaryField.getName(),
                primaryKeyValue);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation handles creating the {@code object} from the
     * correct constructor with the relevant {@link #immutableFields}, calling
     * {@link #finaliseDeserialisation(Database, ResultSet, SerDesable, boolean)}
     * for any additional deserialisation tasks to be handled post-instantiation.
     * </p>
     *
     * @param database The {@link Database} to read from, if required.
     * @param resultSet The {@link ResultSet} to deserialise from.
     * @param careful {@code true} if {@link ForeignField} objects shouldn't be
     *                            set until after its {@link SerDes} is finished
     *                            deserialising (to avoid infinite loops).
     * @return The deserialised {@code object} of type {@link T}.
     * @throws RuntimeException If a {@link Constructor} with the relevant
     *                          {@link ImmutableField} arguments in the relevant
     *                          order doesn't exist; if there was another error
     *                          instantiating the {@code object}.
     */
    @Override
    public T deserialise(Database database, ResultSet resultSet, boolean careful) {
        this.currentlyDeserialising = true;

        final Constructor<T> constructor;

        try {
            // Get the relevant constructor, converting all wrapper classes to their
            // primitive equivalents.
            constructor = this.type.getConstructor(this.immutableFields.stream()
                    .map(field -> field instanceof ForeignField ?
                            ((ForeignField<?, ?, ?>) field).getForeignField()
                                    .getParentType() :
                            PrimitiveClass.convert(field.getType()))
                    .toArray(Class<?>[]::new));
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(NoSuchConstructorException.from(e));
        }

        try {
            // Construct the object and add it to the set of loaded objects.
            final T constructedObject = constructor.newInstance(this.immutableFields.stream()
                    .map(field -> this.getFieldValue(database, resultSet, field)).toArray());
            this.loadedObjects.add(constructedObject);

            // Finalise deserialisation, then return the result.
            return this.finaliseDeserialisation(database, resultSet, constructedObject, careful);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a {@link Field}'s value from the specified {@link ResultSet} in the
     * specified {@link Database}.
     *
     * <p>Note that the database is only needed for reading from foreign fields,
     * so if the specified {@link Field} is not a {@link ForeignField}, it may be
     * {@code null}.</p>
     *
     * @param database The {@link Database} to read from, if necessary.
     * @param resultSet The {@link ResultSet} to read the value from.
     * @param field The {@link Field} to get the value for.
     * @param <FT> The underlying type of the specified {@link Field}.
     * @return The value of the {@link Field}.
     * @since 0.0.5
     */
    @Nullable
    protected <FT> Object getFieldValue(@Nullable final Database database,
                                        final ResultSet resultSet,
                                        final Field<T, FT> field) {
        return Null.applyOrNull(ResultSetConversions.getValueUnchecked(resultSet,
                field.getName(), field.getType()),
                value -> field instanceof ForeignField ?
                        ((ForeignField<T, FT, ?>) field)
                                .getFromValue(Objects.requireNonNull(database), value)
                        : value);
    }

    /**
     * Finalises deserialisation by running additional tasks after an {@link Object}
     * of type {@link T} has been constructed.
     *
     * @param database The {@link Database} to read from, if required.
     * @param resultSet The {@link ResultSet} the {@code constructedObject} was
     *                  deserialised from.
     * @param constructedObject The constructed {@link Object} of type {@link T}.
     * @param careful {@code true} if {@link ForeignField} objects shouldn't be
     *                            set until after its {@link SerDes} is finished
     *                            deserialising (to avoid infinite loops).
     * @return The {@code constructedObject} for in-line calls.
     */
    protected T finaliseDeserialisation (Database database, ResultSet resultSet,
                                         T constructedObject, boolean careful) {
        this.currentlyDeserialising = false;

        // Accept the next deserialised result consumers and clear them.
        this.nextDeserialisedResultConsumers.forEach(consumer ->
                consumer.accept(constructedObject));
        this.nextDeserialisedResultConsumers.clear();

        return constructedObject;
    }

    protected boolean currentlyCreatingTable;

    /**
     * {@inheritDoc}
     *
     * @param database The {@link Database} to create the {@code table} in.
     */
    @Override
    public void createTable(Database database) {
        this.currentlyCreatingTable = true;
        database.createTableUnchecked(this.table, this.primaryField, this.getFields());
        this.currentlyCreatingTable = false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if an SQL {@code table} is currently being created for
     *         this {@link SerDes}; {@code false} otherwise.
     */
    @Override
    public boolean currentlyCreatingTable() {
        return this.currentlyCreatingTable;
    }

    /**
     * A {@code builder}, allowing for easy creation of an {@link AbstractSerDes}.
     *
     * @param <T> The type of the {@link SerDesable} the {@link AbstractSerDes} is
     *            being created for.
     * @param <PK> The type of the {@link PrimaryField}.
     * @param <SD> The type of {@link AbstractSerDes} being built.
     * @param <B> The type of the builder.
     */
    @SuppressWarnings("unchecked")
    protected abstract static class Builder<T extends SerDesable<T, PK>, PK, SD extends AbstractSerDes<T, PK>, B extends Builder<T, PK, SD, B>> {
        protected final Class<T> type;
        protected final String tableName;

        /** The {@code immutable} {@link Field}s, stored in a {@link LinkedHashMap} to retain the order in which they are added. */
        protected final LinkedHashSet<Field<T, ?>> immutableFields = new LinkedHashSet<>();
        /** The {@link Field}s, stored in a {@link LinkedHashMap} to retain the order in which they are added. */
        protected final LinkedHashSet<Field<T, ?>> fields = new LinkedHashSet<>();

        protected PrimaryField<T, PK> primaryField;

        /**
         * Constructs a new {@link Builder} {@code object} with the specified
         * {@link Class} type and table name.
         *
         * @param type The {@link Class} of the {@link SerDesable} having an
         *             {@link AbstractSerDes} built for it.
         * @param tableName The name of the SQL table.
         */
        protected Builder(Class<T> type, String tableName) {
            this.type = type;
            this.tableName = tableName;
        }

        /**
         * Instantiates a new {@link PrimaryField} {@code object} with the
         * specified parameters, setting it as the {@link PrimaryField} for the
         * {@link AbstractSerDes} to be built.
         *
         * @param name The SQL name for the {@link PrimaryField}.
         * @param fieldType The {@link Class} type the {@link PrimaryField} stores.
         * @param getter A getter {@link Function} for the {@link PrimaryField}
         * @return This {@link Builder} for chaining.
         */
        public B primaryField(final String name, final Class<PK> fieldType, final Function<T, PK> getter) {
            return this.primaryField(new PrimaryField<>(name, this.type, fieldType, getter));
        }

        /**
         * Sets the specified {@link PrimaryField} for the {@link AbstractSerDes}
         * to be built.
         *
         * @param primaryField The {@link PrimaryField} to set
         * @return This {@link Builder} for chaining.
         */
        public B primaryField(final PrimaryField<T, PK> primaryField) {
            this.primaryField = primaryField;
            return this.field(primaryField);
        }

        /**
         * Adds the specified {@link Field} to {@link #fields} (and
         * {@link #immutableFields} if {@link Field#isMutable()} returns
         * {@code false}.
         *
         * @param field The {@link Field} to add.
         * @return This {@link Builder} for chaining.
         */
        public B field(final Field<T, ?> field) {
            if (!field.isMutable())
                this.immutableFields.add(field);
            this.fields.add(field);

            return (B) this;
        }

        /**
         * Constructs a new {@link ImmutableField} with the specified properties
         * and calls {@link #field(Field)} to add it to the list of
         * {@link Field}s for the constructed {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param fieldType The {@link Class} type of the {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FT> The type of the {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FT> B field(final String name, final Class<FT> fieldType, final Function<T, FT> getter) {
            return this.field(new ImmutableField<>(name, this.type, fieldType, false, false, getter));
        }

        /**
         * Constructs a new {@code unique} {@link ImmutableField} with the
         * specified properties and calls {@link #field(Field)} to add it to the
         * list of {@link Field}s for the constructed {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param fieldType The {@link Class} type of the {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FT> The type of the {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FT> B uniqueField(final String name, final Class<FT> fieldType, final Function<T, FT> getter) {
            return this.field(new ImmutableField<>(name, this.type, fieldType, true, false, getter));
        }

        /**
         * Constructs a new {@code nullable} {@link ImmutableField} with the
         * specified properties and calls {@link #field(Field)} to add it to
         * the list of {@link Field}s for the constructed
         * {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param fieldType The {@link Class} type of the {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FT> The type of the {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FT> B nullableField(final String name, final Class<FT> fieldType, final Function<T, FT> getter) {
            return this.field(new ImmutableField<>(name, this.type, fieldType, false, true, getter));
        }

        /**
         * Constructs a new {@link ImmutableForeignField} with the specified
         * properties and calls {@link #field(Field)} to add it to the list of
         * {@link Field}s for the constructed {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param foreignField The linking foreign {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FSD> The type of the foreign {@link SerDesable}.
         * @param <FT> The type of the foreign {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FSD extends SerDesable<FSD, ?>, FT> B field(final String name, final Field<FSD, FT> foreignField, final Function<T, FSD> getter) {
            return this.field(new ImmutableForeignField<>(name, this.type, foreignField, false, false, getter));
        }

        /**
         * Constructs a new {@code unique} {@link ImmutableForeignField} with the
         * specified properties and calls {@link #field(Field)} to add it to the
         * list of {@link Field}s for the constructed {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param foreignField The linking foreign {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FSD> The type of the foreign {@link SerDesable}.
         * @param <FT> The type of the foreign {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FSD extends SerDesable<FSD, ?>, FT> B uniqueField(final String name, final Field<FSD, FT> foreignField, final Function<T, FSD> getter) {
            return this.field(new ImmutableForeignField<>(name, this.type, foreignField, true, false, getter));
        }

        /**
         * Constructs a new {@code nullable} {@link ImmutableForeignField} with the
         * specified properties and calls {@link #field(Field)} to add it to the
         * list of {@link Field}s for the constructed {@link AbstractSerDesable}.
         *
         * @param name The SQL name of the {@link Field}.
         * @param foreignField The linking foreign {@link Field}.
         * @param getter A getter function for the {@link Field}.
         * @param <FSD> The type of the foreign {@link SerDesable}.
         * @param <FT> The type of the foreign {@link Field}.
         * @return This {@link Builder} for chaining.
         */
        public <FSD extends SerDesable<FSD, ?>, FT> B nullableField(final String name, final Field<FSD, FT> foreignField, final Function<T, FSD> getter) {
            return this.field(new ImmutableForeignField<>(name, this.type, foreignField, false, true, getter));
        }

        /**
         * Builds the {@link SerDes} of type {@link SD} from all given properties.
         *
         * <p>{@link #assertPrimaryFieldSet()} should be called prior to
         * construction to disallow {@code null} {@link PrimaryField}s and
         * {@link #register(AbstractSerDes)} after to register the created
         * {@link SerDes}.</p>
         *
         * @return The built {@link SerDes} of type {@link SD}.
         */
        public abstract SD build();

        /**
         * Used to assert that {@link #primaryField} has been set (and is not
         * {@code null}). Generally called by {@link #build()} implementations.
         */
        protected void assertPrimaryFieldSet() {
            if (this.primaryField == null)
                throw new PrimaryFieldUnset("Primary field was not set (or null) in SerDes Builder for '" + this.type.getName() + "'.");
        }

        /**
         * Registers the specified {@link SerDes} to the {@link SerDesRegistry}.
         * Generally called by {@link #build()} implementations before returning
         * the built {@link SerDes}.
         *
         * @param serDes The {@link SerDes} to register.
         * @return The specified {@link SerDes}, for in-line calls.
         */
        public SD register(SD serDes) {
            SerDesRegistry.register(serDes);
            return serDes;
        }

        /**
         * Thrown when the {@link #primaryField} was not set when calling
         * {@link #build()}.
         */
        protected static final class PrimaryFieldUnset extends RuntimeException {

            /**
             * Constructs a {@link PrimaryFieldUnset} with a detail message.
             *
             * @param message The detail message.
             */
            public PrimaryFieldUnset(String message) {
                super(message);
            }

        }
    }

}
