package com.harleyoconnor.serdes.annotation;

import java.lang.annotation.*;

/**
 * An annotation that indicates this field should be treated as {@code unique} in an
 * SQL table.
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Unique {

}
