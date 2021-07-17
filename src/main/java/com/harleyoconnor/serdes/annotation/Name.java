package com.harleyoconnor.serdes.annotation;

import java.lang.annotation.*;

/**
 * An annotation that indicates this field should be serialised and deserialised
 * with the provided name {@link #value()} as its name. This acts as an alternative
 * to the default in which the field name itself is used.
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Name {

    /**
     * Returns the name of the {@code field} when serialising or deserialising.
     *
     * @return The name of the {@code field} when serialising or deserialising.
     */
    String value();

}
