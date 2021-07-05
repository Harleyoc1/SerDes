package com.harleyoconnor.serdes.annotation;

import java.lang.annotation.*;

/**
 * An annotation that indicates this field should be ignored when serialising
 * or deserialising.
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {

}
