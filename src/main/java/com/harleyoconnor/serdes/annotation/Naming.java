package com.harleyoconnor.serdes.annotation;

import com.harleyoconnor.javautilities.convention.NamingConvention;
import com.harleyoconnor.serdes.SerDes;
import com.harleyoconnor.serdes.exception.IllegalElementException;

import java.lang.annotation.*;

/**
 * An annotation that indicates this field, or all member fields of this class or
 * each class of this package should be serialised and deserialised by converting
 * the field name to the provided {@link NamingConvention} based on the name given
 * by {@link #value()}.
 *
 * <p>For example, if {@code @Naming("PascalCase")} was applied to a class
 * with a few fields, when creating a {@link SerDes} each field would have its name
 * converted to use the pascal naming convention.</p>
 *
 * <p>When searching for this annotation, the one applied to the element "closest" to
 * the field will always take precedent. For example, if this annotation is defined on
 * a class and a package, the convention defined by the class will be used. Also note
 * that an explicit {@link Name} annotation applied to a field will always override
 * this annotation, regardless of which element it's applied to.</p>
 *
 * @author Harley O'Connor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PACKAGE})
public @interface Naming {

    /**
     * Returns the name of a {@link NamingConvention} to convert field names to when
     * serialising or deserialising.
     *
     * <p>If a {@link NamingConvention} of the given name does not exist, an
     * {@link IllegalElementException} should be thrown.</p>
     *
     * <p>Note that this expects all field names to follow the default Java naming
     * convention of {@link NamingConvention#CAMEL_CASE}. You shouldn't be using any
     * other naming convention for fields anyway.</p>
     *
     * @return The name of a {@link NamingConvention} to convert field names to when
     *         when serialising or deserialising.
     */
    String value();

}
