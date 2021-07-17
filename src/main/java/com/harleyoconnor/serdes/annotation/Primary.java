package com.harleyoconnor.serdes.annotation;

import java.lang.annotation.*;

/**
 * An annotation that indicates that this field should be the {@code primary} field
 * in an SQL database.
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Primary {

}
