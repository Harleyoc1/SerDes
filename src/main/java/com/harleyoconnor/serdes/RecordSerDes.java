package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.field.Field;
import com.harleyoconnor.serdes.field.ImmutableField;
import com.harleyoconnor.serdes.field.PrimaryField;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SerDes} specifically built for {@link Record} objects, and hence
 * only storing {@link ImmutableField} objects.
 *
 * <p>This {@link SerDes} must be instantiated using {@link ClassSerDes.Builder}.</p>
 *
 * @param <T> The type for which this instance will handle serialisation and deserialisation.
 * @param <PK> The type of the primary field.
 * @author Harley O'Connor
 * @see SerDes
 * @see RecordSerDes
 */
public final class RecordSerDes<T extends Record & SerDesable<T, PK>, PK> extends AbstractSerDes<T, PK> {

    private RecordSerDes(Class<T> type, String name, final PrimaryField<T, PK> primaryField, final LinkedHashSet<ImmutableField<T, ?>> fields) {
        super(type, name, primaryField, fields);
    }

    @Override
    public Set<Field<T, ?>> getFields() {
        return this.getImmutableFields().stream().collect(Collectors.toUnmodifiableSet());
    }

    @SuppressWarnings("unchecked")
    public static class Builder<T extends Record & SerDesable<T, PK>, PK, RSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, RSD, B>> extends AbstractSerDes.Builder<T, PK, RSD, B> {

        public Builder(Class<T> type, String tableName) {
            super(type, tableName);
        }

        @Override
        public RSD build() {
            this.assertPrimaryFieldSet();
            return this.register((RSD) new RecordSerDes<>(this.type, this.tableName, this.primaryField, this.immutableFields));
        }

        public static <T extends Record & SerDesable<T, PK>, PK, CSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(final Class<PK> primaryKeyClass, final Class<T> type) {
            return of(primaryKeyClass, type, type.getSimpleName());
        }

        public static <T extends Record & SerDesable<T, PK>, PK, CSD extends RecordSerDes<T, PK>, B extends Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(@SuppressWarnings("unused") final Class<PK> primaryKeyClass, final Class<T> type, final String tableName) {
            return new Builder<>(type, tableName);
        }

    }

}
