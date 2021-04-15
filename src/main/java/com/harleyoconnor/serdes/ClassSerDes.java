package com.harleyoconnor.serdes;

import com.google.common.collect.ImmutableSet;
import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.field.*;
import com.harleyoconnor.serdes.util.ResultSetConversions;

import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Main implementation of {@link SerDes}, holding an {@link ImmutableSet} of {@link Field}
 * objects - meaning they can either be a hard or soft field.
 *
 * <p>This {@link SerDes} must be instantiated using {@link Builder}.</p>
 *
 * @param <T> The type for which this instance will handle serialisation and deserialisation.
 * @param <PK> The type of the primary field.
 *
 * @author Harley O'Connor
 * @see SerDes
 * @see RecordSerDes
 */
public final class ClassSerDes<T extends SerDesable<T, PK>, PK> extends AbstractSerDes<T, PK> {

    private final LinkedHashSet<Field<T, ?>> fields;

    private ClassSerDes(final Class<T> type, final String table, final PrimaryField<T, PK> primaryField, final LinkedHashSet<Field<T, ?>> fields, final LinkedHashSet<ImmutableField<T, ?>> immutableFields) {
        super(type, table, primaryField, immutableFields);
        this.fields = fields;
    }

    @Override
    public Set<Field<T, ?>> getFields() {
        return fields;
    }

    @Override
    public void createTable(Database database) {
        this.currentlyCreatingTable = true;
        database.createTableUnchecked(this.table, this.primaryField, this.fields);
        this.currentlyCreatingTable = false;
    }

    @Override
    protected T finaliseDeserialisation(Database database, ResultSet resultSet, T constructedObject, boolean careful) {
        this.getMutableFields().forEach(mutableField -> this.setField(database, resultSet, constructedObject, mutableField));

        if (!careful)
            this.getForeignFields().forEach(foreignField -> this.setField(database, resultSet, constructedObject, foreignField));
        else this.getForeignFields().forEach(foreignField -> this.setWhenNextDeserialised(database, constructedObject, foreignField, foreignField.getForeignField()));

        return super.finaliseDeserialisation(database, resultSet, constructedObject, careful);
    }

    private <A extends SerDesable<A, ?>, B, C> void setWhenNextDeserialised(final Database database, final T constructedObject, final ForeignField<T, C, ?> foreignField, final Field<A, B> foreignForeignField) {
        foreignForeignField.getParentSerDes().get().whenNextDeserialised(deserialisedObject -> this.whenNextDeserialised(database, constructedObject, deserialisedObject, foreignField));
    }

    @SuppressWarnings("unchecked")
    private <SD extends SerDesable<SD, ?>, V> void whenNextDeserialised(final Database database, final T object, final SD deserialisedObject, final ForeignField<T, V, ?> foreignField) {
        deserialisedObject.getSerDes().getFields().stream().filter(field -> field.equals(foreignField.getForeignField())).map(field -> ((Field<SD, V>) field)).forEach(field ->
                foreignField.set(database, object, field.get(deserialisedObject))
        );
    }

    private <V> void setField(final Database database, final ResultSet resultSet, final T object, final Field<T, V> field) {
        field.set(database, object, ResultSetConversions.getValueUnchecked(resultSet, field.getName(), field.getType()));
    }

    @SuppressWarnings("unchecked")
    public static class Builder<T extends SerDesable<T, PK>, PK, CSD extends ClassSerDes<T, PK>, B extends ClassSerDes.Builder<T, PK, CSD, B>> extends AbstractSerDes.Builder<T, PK, CSD, B> {

        public Builder(final Class<T> type, final String tableName) {
            super(type, tableName);
        }

        public <FT> B field(final String name, final Class<FT> fieldType, final Function<T, FT> getter, final BiConsumer<T, FT> setter) {
            return this.field(new MutableField<>(name, this.type, fieldType, false, false, getter, setter));
        }

        public <FT> B uniqueField(final String name, final Class<FT> fieldType, final Function<T, FT> getter, final BiConsumer<T, FT> setter) {
            return this.field(new MutableField<>(name, this.type, fieldType, true, false, getter, setter));
        }

        public <FT> B nullableField(final String name, final Class<FT> fieldType, final Function<T, FT> getter, final BiConsumer<T, FT> setter) {
            return this.field(new MutableField<>(name, this.type, fieldType, false, true, getter, setter));
        }

        public <FKT extends SerDesable<FKT, ?>, FT> B field(final String name, final Field<FKT, FT> foreignField, final Function<T, FKT> getter, final BiConsumer<T, FKT> setter) {
            return this.field(new MutableForeignField<>(name, this.type, foreignField, false, false, getter, setter));
        }

        public <FKT extends SerDesable<FKT, ?>, FT> B uniqueField(final String name, final Field<FKT, FT> foreignField, final Function<T, FKT> getter, final BiConsumer<T, FKT> setter) {
            return this.field(new MutableForeignField<>(name, this.type, foreignField, true, false, getter, setter));
        }

        public <FKT extends SerDesable<FKT, ?>, FT> B nullableField(final String name, final Field<FKT, FT> foreignField, final Function<T, FKT> getter, final BiConsumer<T, FKT> setter) {
            return this.field(new MutableForeignField<>(name, this.type, foreignField, false, true, getter, setter));
        }

        @Override
        public CSD build () {
            this.assertPrimaryFieldSet();
            return this.register((CSD) new ClassSerDes<>(this.type, this.tableName, this.primaryField, this.fields, this.immutableFields));
        }

        public static <T extends SerDesable<T, PK>, PK, CSD extends ClassSerDes<T, PK>, B extends ClassSerDes.Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(final Class<T> type, final Class<PK> primaryKeyClass) {
            return of(type, primaryKeyClass, type.getSimpleName() + "s");
        }

        public static <T extends SerDesable<T, PK>, PK, CSD extends ClassSerDes<T, PK>, B extends ClassSerDes.Builder<T, PK, CSD, B>> Builder<T, PK, CSD, B> of(final Class<T> type, final Class<PK> primaryKeyClass, final String tableName) {
            return new Builder<>(type, tableName);
        }

    }

}
