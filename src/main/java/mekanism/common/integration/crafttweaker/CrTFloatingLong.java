package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_FLOATING_LONG)
public class CrTFloatingLong {

    /**
     * Creates a wrapper for our {@link FloatingLong} number type out of a long.
     *
     * @param value Long to convert
     */
    @ZenCodeType.Method
    public static CrTFloatingLong create(long value) {
        //TODO - 10.1: Decide if we want this to be create(@ZenCodeType.Unsigned long value)
        // so that it is then an unsigned long given that is what floating longs can handle
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return new CrTFloatingLong(FloatingLong.createConst(value));
    }

    /**
     * Creates a wrapper for our {@link FloatingLong} number type out of a double.
     *
     * @param value Double to convert
     */
    @ZenCodeType.Method
    public static CrTFloatingLong create(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return new CrTFloatingLong(FloatingLong.createConst(value));
    }

    /**
     * Creates a wrapper for our {@link FloatingLong} number type out of a string.
     *
     * @param value String to parse
     */
    @ZenCodeType.Method
    public static CrTFloatingLong create(String value) {
        return new CrTFloatingLong(FloatingLong.parseFloatingLong(value, true));
    }

    private final FloatingLong internal;

    private CrTFloatingLong(FloatingLong internal) {
        this.internal = internal;
    }

    public FloatingLong getInternalAsConst() {
        //Ensure that the floating long we put into the recipe is a constant, as it may not be if operators were used on it
        return internal.copyAsConst();
    }

    /**
     * Converts this floating long to a string
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public String asString() {
        return internal.toString();
    }

    /**
     * Adds the given {@link CrTFloatingLong} to this {@link CrTFloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The {@link CrTFloatingLong} to add.
     *
     * @return The {@link CrTFloatingLong} representing the value of adding the given {@link CrTFloatingLong} to this {@link CrTFloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.ADD)
    public CrTFloatingLong add(CrTFloatingLong toAdd) {
        return new CrTFloatingLong(internal.add(toAdd.internal));
    }

    /**
     * Subtracts the given {@link CrTFloatingLong} from this {@link CrTFloatingLong} and returns the result in a new object. This gets clamped at the lower bound of
     * {@link FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The {@link CrTFloatingLong} to subtract.
     *
     * @return The {@link CrTFloatingLong} representing the value of subtracting the given {@link CrTFloatingLong} from this {@link CrTFloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.SUB)
    public CrTFloatingLong subtract(CrTFloatingLong toSubtract) {
        return new CrTFloatingLong(internal.subtract(toSubtract.internal));
    }

    /**
     * Multiplies the given {@link CrTFloatingLong} with this {@link CrTFloatingLong} and returns the result in a new object. This gets clamped at the upper bound of
     * {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The {@link CrTFloatingLong} to multiply by.
     *
     * @return The {@link CrTFloatingLong} representing the value of multiplying the given {@link CrTFloatingLong} with this {@link CrTFloatingLong}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public CrTFloatingLong multiply(CrTFloatingLong toMultiply) {
        return new CrTFloatingLong(internal.multiply(toMultiply.internal));
    }

    /**
     * Divides this {@link CrTFloatingLong} by the given {@link CrTFloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The {@link CrTFloatingLong} to divide by.
     *
     * @return The {@link CrTFloatingLong} representing the value of dividing this {@link CrTFloatingLong} by the given {@link CrTFloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.DIV)
    public CrTFloatingLong divide(CrTFloatingLong toDivide) {
        return new CrTFloatingLong(internal.divide(toDivide.internal));
    }

    /**
     * Checks if this {@link CrTFloatingLong} is equal to the given {@link CrTFloatingLong}.
     *
     * @param toCompare The {@link CrTFloatingLong} to compare to.
     *
     * @return {@code true} if this {@link CrTFloatingLong} is equal to the given {@link CrTFloatingLong}, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
    public boolean isEqual(CrTFloatingLong toCompare) {
        return internal.equals(toCompare.internal);
    }

    /**
     * Compares this {@link CrTFloatingLong} to the given {@link CrTFloatingLong}.
     *
     * @param toCompare The {@link CrTFloatingLong} to compare to.
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
    public int compareTo(CrTFloatingLong toCompare) {
        return internal.compareTo(toCompare.internal);
    }
}