package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = FloatingLong.class, zenCodeName = CrTConstants.CLASS_FLOATING_LONG)
public class CrTFloatingLong {

    private CrTFloatingLong() {
    }

    /**
     * Creates a {@link FloatingLong} representing the given long.
     *
     * @param value Long to convert
     */
    @ZenCodeType.StaticExpansionMethod
    public static FloatingLong create(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return FloatingLong.createConst(value);
    }

    /**
     * Creates a {@link FloatingLong} representing the given unsigned long.
     *
     * @param value Unsigned long to convert
     */
    @ZenCodeType.StaticExpansionMethod
    public static FloatingLong createFromUnsigned(@ZenCodeType.Unsigned long value) {
        return FloatingLong.createConst(value);
    }

    /**
     * Creates a {@link FloatingLong} representing the given double.
     *
     * @param value Double to convert
     */
    @ZenCodeType.StaticExpansionMethod
    public static FloatingLong create(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return FloatingLong.createConst(value);
    }

    /**
     * Creates a {@link FloatingLong} representing the given string representation.
     *
     * @param value String to parse
     */
    @ZenCodeType.StaticExpansionMethod
    public static FloatingLong create(String value) {
        return FloatingLong.parseFloatingLong(value, true);
    }

    /**
     * Converts this {@link FloatingLong} to a string.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static String asString(FloatingLong _this) {
        return _this.toString();
    }

    /**
     * Adds the given {@link FloatingLong} to this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The {@link FloatingLong} to add.
     *
     * @return The {@link FloatingLong} representing the value of adding the given {@link FloatingLong} to this {@link FloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.ADD)
    public static FloatingLong add(FloatingLong _this, FloatingLong toAdd) {
        return _this.add(toAdd);
    }

    /**
     * Subtracts the given {@link FloatingLong} from this {@link FloatingLong} and returns the result in a new object. This gets clamped at the lower bound of {@link
     * FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The {@link FloatingLong} to subtract.
     *
     * @return The {@link FloatingLong} representing the value of subtracting the given {@link FloatingLong} from this {@link FloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.SUB)
    public static FloatingLong subtract(FloatingLong _this, FloatingLong toSubtract) {
        return _this.subtract(toSubtract);
    }

    /**
     * Multiplies the given {@link FloatingLong} with this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The {@link FloatingLong} to multiply by.
     *
     * @return The {@link FloatingLong} representing the value of multiplying the given {@link FloatingLong} with this {@link FloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static FloatingLong multiply(FloatingLong _this, FloatingLong toMultiply) {
        return _this.multiply(toMultiply);
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return The {@link FloatingLong} representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.DIV)
    public static FloatingLong divide(FloatingLong _this, FloatingLong toDivide) {
        return _this.divide(toDivide);
    }

    /**
     * Checks if this {@link FloatingLong} is equal to the given {@link FloatingLong}.
     *
     * @param toCompare The {@link FloatingLong} to compare to.
     *
     * @return {@code true} if this {@link FloatingLong} is equal to the given {@link FloatingLong}, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
    public static boolean isEqual(FloatingLong _this, FloatingLong toCompare) {
        return _this.equals(toCompare);
    }

    /**
     * Compares this {@link FloatingLong} to the given {@link FloatingLong}.
     *
     * @param toCompare The {@link FloatingLong} to compare to.
     *
     * @return zero if equal to toCompare
     * <br>
     * less than zero if smaller than toCompare
     * <br>
     * greater than zero if bigger than toCompare
     *
     * @implNote {@code 1} or {@code -1} if the overall value is different
     * <br>
     * {@code 2} or {@code -2} if the value is the same but the decimal is different
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.COMPARE)
    public static int compareTo(FloatingLong _this, FloatingLong toCompare) {
        return _this.compareTo(toCompare);
    }

    /**
     * @param other The {@link FloatingLong} to compare to
     *
     * @return this {@link FloatingLong} if it is greater than equal to the given {@link FloatingLong}, otherwise returns the given {@link FloatingLong}
     *
     * @implNote This method does not copy the value that is returned, so it is on the caller to keep track of mutability.
     */
    @ZenCodeType.Method
    public static FloatingLong max(FloatingLong _this, FloatingLong other) {
        return _this.max(other);
    }

    /**
     * @param other The {@link FloatingLong} to compare to
     *
     * @return this {@link FloatingLong} if it is smaller than equal to the given {@link FloatingLong}, otherwise returns the given {@link FloatingLong}
     *
     * @implNote This method does not copy the value that is returned, so it is on the caller to keep track of mutability.
     */
    @ZenCodeType.Method
    public static FloatingLong min(FloatingLong _this, FloatingLong other) {
        return _this.min(other);
    }

    /**
     * Returns the smallest {@link FloatingLong} that is greater than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @return the smallest {@link FloatingLong} that is greater than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @implNote If this {@link FloatingLong} is already equal to a mathematical unsigned long, then the result is the same as the argument. Additionally, if this {@link
     * FloatingLong} is larger than the maximum unsigned long, this instead returns a {@link FloatingLong} representing the maximum unsigned long.
     */
    @ZenCodeType.Method
    public static FloatingLong ceil(FloatingLong _this) {
        return _this.ceil().copyAsConst();
    }

    /**
     * Returns the largest {@link FloatingLong} that is less than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @return the largest {@link FloatingLong} that is less than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @implNote If this {@link FloatingLong} is already equal to a mathematical unsigned long, then the result is the same as the argument.
     */
    @ZenCodeType.Method
    public static FloatingLong floor(FloatingLong _this) {
        return _this.floor().copyAsConst();
    }

    /**
     * Gets the "byte" representation of this {@link FloatingLong}.
     *
     * @implNote We clamp the value to MAX_BYTE rather than having it overflow into the negatives.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static byte byteValue(FloatingLong _this) {
        return _this.byteValue();
    }

    /**
     * Gets the "short" representation of this {@link FloatingLong}.
     *
     * @implNote We clamp the value to MAX_SHORT rather than having it overflow into the negatives.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static short shortValue(FloatingLong _this) {
        return _this.shortValue();
    }

    /**
     * Gets the "int" representation of this {@link FloatingLong}.
     *
     * @implNote We clamp the value to MAX_INT rather than having it overflow into the negatives.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static int intValue(FloatingLong _this) {
        return _this.intValue();
    }

    /**
     * Gets the "long" representation of this {@link FloatingLong}.
     *
     * @implNote We clamp the value to MAX_LONG rather than having it overflow into the negatives and being unsigned.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static long longValue(FloatingLong _this) {
        return _this.longValue();
    }

    /**
     * Gets the "float" representation of this {@link FloatingLong}.
     *
     * Converts the unsigned long portion to a float in the same way Guava's UnsignedLong does, and then adds our decimal portion
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static float floatValue(FloatingLong _this) {
        return _this.floatValue();
    }

    /**
     * Gets the "double" representation of this {@link FloatingLong}.
     *
     * Converts the unsigned long portion to a double in the same way Guava's UnsignedLong does, and then adds our decimal portion
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster
    public static double doubleValue(FloatingLong _this) {
        return _this.doubleValue();
    }
}