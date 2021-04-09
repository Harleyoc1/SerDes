package com.harleyoconnor.serdes;

import com.harleyoconnor.serdes.field.Field;
import com.harleyoconnor.serdes.field.ForeignField;

import java.util.Objects;

/**
 * A base implementation of {@link SerDesable}, providing common implementations of {@link #toString()},
 * {@link #equals(Object)}, and {@link #hashCode()}.
 *
 * @param <T> The extending {@link Class} type of this.
 * @param <PK> The {@code primary key} type.
 * @author Harley O'Connor
 * @see SerDesable
 */
@SuppressWarnings("unchecked")
public abstract class AbstractSerDesable<T extends AbstractSerDesable<T, PK>, PK> implements SerDesable<T, PK> {

    @Override
    public String toString() {
        return this.getSerDes().toString((T) this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        final var otherSerDesable = ((AbstractSerDesable<T, PK>) obj);

        // SerDes instances should be created as a static field.
        if (this.getSerDes() != otherSerDesable.getSerDes())
            return false;

        for (final Field<T, ?> field : this.getSerDes().getFields()) {
            if (!Objects.equals(field.get((T) this), field.get((T) otherSerDesable))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSerDes().getFields().stream()
                .filter(field -> !(field instanceof ForeignField && ((ForeignField<T, ?, ?>) field).getActual((T) this) == null))
                .map(field -> field.get((T) this)).toArray());
    }

}
