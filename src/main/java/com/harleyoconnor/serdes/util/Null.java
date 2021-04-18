package com.harleyoconnor.serdes.util;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
// TODO: Extend capabilities, add Javadoc, and move to JavaUtilities.
public final class Null {

    @Nullable
    public static <T, R> R applyOrNull(@Nullable final T object, final Function<T, R> function) {
        return object == null ? null : function.apply(object);
    }

}
