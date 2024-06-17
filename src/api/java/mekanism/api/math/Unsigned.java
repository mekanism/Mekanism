package mekanism.api.math;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Specifies that the annotated long is (or is to be) treated as unsigned.
 * Authors must take care to perform unsigned-safe maths on the value.
 *
 * @see Long#divideUnsigned(long, long)
 * @see Long#compareUnsigned(long, long)
 * @see Long#toUnsignedString(long)
 * @see ULong#max(long, long)
 * @see ULong#min(long, long)
 */
@Documented
@Target(ElementType.TYPE_USE)
public @interface Unsigned {
}
