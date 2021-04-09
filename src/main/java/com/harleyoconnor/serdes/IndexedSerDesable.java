package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.database.Database;
import com.harleyoconnor.serdes.field.PrimaryField;

/**
 * An {@code abstract} extension of {@link AbstractSerDesable} accommodating {@link SerDesable}s
 * whose {@link PrimaryField} is an {@link Integer} {@code id}. The {@link PrimaryField}
 * {@link Object} can be created by {@link #createPrimaryField(Class)}.
 *
 * @author Harley O'Connor
 */
public abstract class IndexedSerDesable<T extends IndexedSerDesable<T>> extends AbstractSerDesable<T, Integer> {

    protected static <T extends IndexedSerDesable<T>> PrimaryField<T, Integer> createPrimaryField(final Class<T> extendingClass) {
        return createPrimaryField("id", extendingClass);
    }

    protected static <T extends IndexedSerDesable<T>> PrimaryField<T, Integer> createPrimaryField(final String name, final Class<T> extendingClass) {
        return new PrimaryField<>(name, extendingClass, Integer.class, IndexedSerDesable::getId);
    }

    protected final int id;

    public IndexedSerDesable(final Database database) {
        this.id = database.getMaxUnsafe(this.getSerDes().getTable(), this.getPrimaryField().getName());
    }

    public IndexedSerDesable(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
