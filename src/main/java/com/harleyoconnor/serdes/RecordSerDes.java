package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.field.Field;
import com.harleyoconnor.serdes.field.ImmutableField;
import com.harleyoconnor.serdes.field.PrimaryField;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of {@link SerDes} specifically built for {@link Record} objects, and hence
 * only storing {@link ImmutableField} objects.
 *
 * @param <T> The type for which this instance will handle serialisation and deserialisation.
 * @param <PK> The type of the primary field.
 *
 * @author Harley O'Connor
 * @see SerDes
 * @see RecordSerDes
 */
public final class RecordSerDes<T extends Record & SerDesable<T, PK>, PK> extends AbstractSerDes<T, PK> {

    /**
     * Constructs a new {@link RecordSerDes} with the specified {@link SerDesable}
     * {@link Class}, SQL table name, {@link PrimaryField}, and other {@link Field}s.
     *
     * <p>Has {@code private} access as it is intended to be constructed with
     * {@link Builder}.</p>
     *
     * @param type The {@link Class} of the {@link SerDesable} this {@link SerDes} will
     *             handle.
     * @param name The name of the SQL table.
     * @param primaryField The {@link PrimaryField} of the SQL table.
     * @param fields All {@link Field} {@code object}s for this {@link RecordSerDes}.
     */
    private RecordSerDes(Class<T> type, String name, final PrimaryField<T, PK> primaryField, final LinkedHashSet<Field<T, ?>> fields) {
        super(type, name, primaryField, fields);
    }

    /**
     * {@inheritDoc}
     *
     * @return The result of {@link #getImmutableFields()} (not collected as that is already
     *         handled by this method.
     */
    @Override
    public Set<Field<T, ?>> getFields() {
        return this.getImmutableFields();
    }

    /**
     * A {@link AbstractSerDes.Builder} extension for constructing {@link RecordSerDes}
     * {@code object}s.
     *
     * <p>A typical use for this may look something like below:</p><pre>
     *     public static final SerDes{@literal <} Rectangle, Integer{@literal >} SER_DES = RecordSerDes.Builder.of(Integer.class, Rectangle.class)
     *         .field("id", Integer.class, Rectangle::id)
     *         .field("length", Double.class, Rectangle::length)
     *         .field("width", Double.class, Rectangle::width).build();
     * </pre>
     *
     * @param <T> The type of {@link SerDesable} the {@link RecordSerDes} is being created
     *            for.
     * @param <PK> The type of the {@link PrimaryField}.
     * @param <RSD> The type of {@link RecordSerDes} being built.
     * @param <B> The type of the builder.
     */
    @SuppressWarnings("unchecked")
    public static class Builder<T extends Record & SerDesable<T, PK>, PK, RSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, RSD, B>> extends AbstractSerDes.Builder<T, PK, RSD, B> {

        /**
         * Constructs a new {@link Builder} {@code object} with the specified
         * {@link Class} type and table name.
         *
         * <p>For external construction, {@link #of(Class, Class, String)} should be
         * used.</p>
         *
         * @param type The {@link Class} of the {@link SerDesable} having an
         *             {@link RecordSerDes} built for it.
         * @param tableName The name of the SQL table.
         */
        protected Builder(Class<T> type, String tableName) {
            super(type, tableName);
        }

        /**
         * Constructs a new {@link RecordSerDes} from the data given to this
         * {@link Builder} class.
         *
         * @return The built {@link RecordSerDes}.
         * @throws PrimaryFieldUnset If a {@link PrimaryField} was
         *         not set for this {@link Builder}.
         */
        @Override
        public RSD build() {
            this.assertPrimaryFieldSet();
            return this.register((RSD) new RecordSerDes<>(this.type, this.tableName, this.primaryField, this.immutableFields));
        }

        /**
         * Constructs a new {@link Builder} {@code object} with the specified
         * {@link Class} type. The name will be the simple name of the specified
         * {@link Class} with {@code s} added to the end.
         *
         * @param primaryFieldClass The {@link Class} of the value for the
         *                          {@link PrimaryField}.
         * @param type The {@link Class} of the {@link SerDesable} having an
         *             {@link RecordSerDes} built for it.
         * @param <T> The type of {@link SerDesable} the {@link RecordSerDes} is being created
         *            for.
         * @param <PK> The type of the {@link PrimaryField}.
         * @param <B> The type of the builder.
         */
        public static <T extends Record & SerDesable<T, PK>, PK, CSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(final Class<PK> primaryFieldClass, final Class<T> type) {
            return of(primaryFieldClass, type, type.getSimpleName());
        }

        /**
         * Constructs a new {@link Builder} {@code object} with the specified
         * {@link Class} type and table name.
         *
         * @param primaryFieldClass The {@link Class} of the value for the
         *                          {@link PrimaryField}.
         * @param type The {@link Class} of the {@link SerDesable} having an
         *             {@link RecordSerDes} built for it.
         * @param table The name of the SQL table.
         */
        public static <T extends Record & SerDesable<T, PK>, PK, CSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(@SuppressWarnings("unused") final Class<PK> primaryFieldClass, final Class<T> type, final String table) {
            return new Builder<>(type, table);
        }

    }

}
