package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
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
     * Converts this floating long to a string
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
}