package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.network.PacketBuffer;

/**
 * A class representing a positive number with an internal value defined by an unsigned long, and a floating point number stored in a short
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingLong extends Number implements Comparable<FloatingLong> {

    private static final DecimalFormat df;

    static {
        df = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    }

    //TODO: Eventually we should define a way of doing a set of operations all at once, and outputting a new value
    // given that way we can internally do all the calculations using primitives rather than spamming a lot of objects
    /**
     * The maximum number of decimal digits we can represent
     */
    private static final int DECIMAL_DIGITS = 4;
    /**
     * The maximum value we can represent as a decimal
     */
    private static final short MAX_DECIMAL = 9_999;
    /**
     * The value which represents 1.0, this is one more than the value of {@link #MAX_DECIMAL}
     */
    private static final short SINGLE_UNIT = MAX_DECIMAL + 1;
    /**
     * The maximum value where the decimal can be eliminated without {@link #value} overflowing, want to be able to shift twice
     */
    private static final long MAX_LONG_SHIFT = Long.divideUnsigned(Long.divideUnsigned(-1L, SINGLE_UNIT), SINGLE_UNIT);
    /**
     * A constant holding the value {@code 0}
     */
    public static final FloatingLong ZERO = createConst(0);
    /**
     * A constant holding the value {@code 1}
     */
    public static final FloatingLong ONE = createConst(1);
    /**
     * A constant holding the maximum value for a {@link FloatingLong}
     */
    public static final FloatingLong MAX_VALUE = createConst(-1, MAX_DECIMAL);
    /**
     * A constant holding the maximum value for a {@link FloatingLong} represented as a double
     */
    private static final double MAX_AS_DOUBLE = Double.parseDouble(MAX_VALUE.toString());

    /**
     * Creates a mutable {@link FloatingLong} from a given primitive double.
     *
     * @param value The value to represent as a {@link FloatingLong}
     *
     * @return A mutable {@link FloatingLong} from a given primitive double.
     *
     * @apiNote If this method is called with negative numbers it will be clamped to {@link #ZERO}. If this is called with a value larger than {@link #MAX_VALUE}, it will
     * instead be clamped to {@link #MAX_VALUE}.
     * @implNote Does not round double value, and instead just drops any trailing digits
     */
    public static FloatingLong create(double value) {
        if (value > MAX_AS_DOUBLE) {
            return MAX_VALUE;
        } else if (value < 0) {
            return ZERO;
        }
        long lValue = (long) value;
        short decimal = parseDecimal(df.format(value));
        return create(lValue, decimal);
    }

    /**
     * Creates a mutable {@link FloatingLong} from a given primitive unsigned long.
     *
     * @param value The value to use for the whole number portion of the {@link FloatingLong}
     *
     * @return A mutable {@link FloatingLong} from a given primitive long.
     */
    public static FloatingLong create(long value) {
        return create(value, (short) 0);
    }

    /**
     * Creates a mutable {@link FloatingLong} from a given primitive unsigned long, and decimal represented as a short.
     *
     * @param value   The value to use for the whole number portion of the {@link FloatingLong}
     * @param decimal The short value to use for the decimal portion of the {@link FloatingLong}
     *
     * @return A mutable {@link FloatingLong} from a given primitive long, and short.
     *
     * @apiNote If this method is called with negative numbers for {@code decimal} it will be clamped to zero.
     */
    public static FloatingLong create(long value, short decimal) {
        return new FloatingLong(value, decimal, false);
    }

    /**
     * Creates a constant {@link FloatingLong} from a given primitive double.
     *
     * @param value The value to represent as a {@link FloatingLong}
     *
     * @return A constant {@link FloatingLong} from a given primitive double.
     *
     * @apiNote If this method is called with negative numbers it will be clamped to {@link #ZERO}. If this is called with a value larger than {@link #MAX_VALUE}, it will
     * instead be clamped to {@link #MAX_VALUE}.
     * @implNote Does not round double value, and instead just drops any trailing digits
     */
    public static FloatingLong createConst(double value) {
        if (value > MAX_AS_DOUBLE) {
            return MAX_VALUE;
        } else if (value < 0) {
            return ZERO;
        }
        long lValue = (long) value;
        short decimal = parseDecimal(df.format(value));
        return createConst(lValue, decimal);
    }

    /**
     * Creates a constant {@link FloatingLong} from a given primitive unsigned long.
     *
     * @param value The value to use for the whole number portion of the {@link FloatingLong}
     *
     * @return A constant {@link FloatingLong} from a given primitive long.
     */
    public static FloatingLong createConst(long value) {
        return createConst(value, (short) 0);
    }

    /**
     * Creates a constant {@link FloatingLong} from a given primitive unsigned long, and decimal represented as a short.
     *
     * @param value   The value to use for the whole number portion of the {@link FloatingLong}
     * @param decimal The short value to use for the decimal portion of the {@link FloatingLong}
     *
     * @return A constant {@link FloatingLong} from a given primitive long, and short.
     *
     * @apiNote If this method is called with negative numbers for {@code decimal} it will be clamped to zero.
     */
    public static FloatingLong createConst(long value, short decimal) {
        return new FloatingLong(value, decimal, true);
    }

    /**
     * Reads a mutable {@link FloatingLong} from a buffer
     *
     * @param buffer The {@link PacketBuffer} to read from
     *
     * @return A mutable {@link FloatingLong}
     */
    public static FloatingLong readFromBuffer(PacketBuffer buffer) {
        return new FloatingLong(buffer.readVarLong(), buffer.readShort(), false);
    }

    private final boolean isConstant;
    private long value;
    private short decimal;

    private FloatingLong(long value, short decimal, boolean isConstant) {
        setAndClampValues(value, decimal);
        //Set the constant state after we have updated the values
        this.isConstant = isConstant;
    }

    /**
     * @return the unsigned long representing the whole number value of this {@link FloatingLong}
     */
    public long getValue() {
        return value;
    }

    /**
     * @return the short representing the decimal value of this {@link FloatingLong}
     */
    public short getDecimal() {
        return decimal;
    }

    /**
     * Sets the internal value and decimal to the given values, clamping them so that {@code value} is not negative, and {@code decimal} is not negative or greater than
     * {@link #MAX_DECIMAL}. If this {@link FloatingLong} is constant, it returns a new object otherwise it returns this {@link FloatingLong} after updating the internal
     * values.
     *
     * @param value   The whole number value to set
     * @param decimal The decimal value to set
     *
     * @return If this {@link FloatingLong} is constant, it returns a new object otherwise it returns this {@link FloatingLong} after updating the internal values.
     */
    private FloatingLong setAndClampValues(long value, short decimal) {
        if (decimal < 0) {
            decimal = 0;
        } else if (decimal > MAX_DECIMAL) {
            decimal = MAX_DECIMAL;
        }
        if (isConstant) {
            return create(value, decimal);
        }
        this.value = value;
        this.decimal = decimal;
        return this;
    }

    /**
     * Checks if this {@link FloatingLong} is zero. This includes checks for if somehow the internal values have become negative.
     *
     * @return {@code true} if this {@link FloatingLong} should be treated as zero, {@code false} otherwise.
     */
    public boolean isZero() {
        return value == 0 && decimal <= 0;
    }

    /**
     * Copies this {@link FloatingLong}, into a mutable {@link FloatingLong}
     */
    public FloatingLong copy() {
        return new FloatingLong(value, decimal, false);
    }

    /**
     * Copies this {@link FloatingLong}, into a constant {@link FloatingLong}. If the current {@link FloatingLong{ is already a constant just returns self.
     */
    public FloatingLong copyAsConst() {
        return isConstant ? this : new FloatingLong(value, decimal, true);
    }

    /**
     * Adds the given {@link FloatingLong} to this {@link FloatingLong}, modifying the current object unless it is a constant in which case it instead returns the result
     * in a new object. This gets clamped at the upper bound of {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The {@link FloatingLong} to add.
     *
     * @return The {@link FloatingLong} representing the value of adding the given {@link FloatingLong} to this {@link FloatingLong}.
     *
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}
     * <br>
     * {@code value = value.plusEqual(toAdd)}
     */
    public FloatingLong plusEqual(FloatingLong toAdd) {
        long newValue;
        short newDecimal;
        if ((value < 0 && toAdd.value < 0) || ((value < 0 || toAdd.value < 0) && (value + toAdd.value >= 0))) {
            return setAndClampValues(-1, MAX_DECIMAL);
        }
        newValue = value + toAdd.value;
        newDecimal = (short) (decimal + toAdd.decimal);
        if (newDecimal > MAX_DECIMAL) {
            if (newValue == -1) {
                newDecimal = MAX_DECIMAL;
            } else {
                newDecimal -= SINGLE_UNIT;
                newValue++;
            }
        }
        return setAndClampValues(newValue, newDecimal);
    }

    /**
     * Subtracts the given {@link FloatingLong} from this {@link FloatingLong}, modifying the current object unless it is a constant in which case it instead returns the
     * result in a new object. This gets clamped at the lower bound of {@link FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The {@link FloatingLong} to subtract.
     *
     * @return The {@link FloatingLong} representing the value of subtracting the given {@link FloatingLong} from this {@link FloatingLong}.
     *
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}
     * <br>
     * {@code value = value.minusEqual(toSubtract)}
     */
    public FloatingLong minusEqual(FloatingLong toSubtract) {
        if (toSubtract.greaterThan(this)) {
            //Clamp the result at zero as floating longs cannot become negative
            return setAndClampValues(0, (short) 0);
        }
        long newValue = value - toSubtract.value;
        short newDecimal = (short) (decimal - toSubtract.decimal);
        if (newDecimal < 0) {
            newDecimal += SINGLE_UNIT;
            newValue--;
        }
        return setAndClampValues(newValue, newDecimal);
    }

    /**
     * Multiplies the given {@link FloatingLong} with this {@link FloatingLong}, modifying the current object unless it is a constant in which case it instead returns the
     * result in a new object. This gets clamped at the upper bound of {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The {@link FloatingLong} to multiply by.
     *
     * @return The {@link FloatingLong} representing the value of multiplying the given {@link FloatingLong} with this {@link FloatingLong}.
     *
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}
     * <br>
     * {@code value = value.timesEqual(toMultiply)}
     */
    public FloatingLong timesEqual(FloatingLong toMultiply) {
        //(a+b)*(c+d) where numbers represent decimal, numbers represent value
        if (multiplyLongsWillOverFlow(value, toMultiply.value)) {
            return MAX_VALUE;
        }
        FloatingLong temp = create(multiplyLongs(value, toMultiply.value));//a * c
        temp = temp.plusEqual(multiplyLongAndDecimal(value, toMultiply.decimal));//a * d
        temp = temp.plusEqual(multiplyLongAndDecimal(toMultiply.value, decimal));//b * c
        temp = temp.plusEqual(multiplyDecimals(decimal, toMultiply.decimal));//b * d
        return setAndClampValues(temp.value, temp.decimal);
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong}, modifying the current object unless it is a constant in which case it instead returns the
     * result in a new object. This gets clamped at the upper bound of {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return The {@link FloatingLong} representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}
     * <br>
     * {@code value = value.divideEquals(toDivide)}
     */
    public FloatingLong divideEquals(FloatingLong toDivide) {
        if (toDivide.isZero()) {
            throw new ArithmeticException("Division by zero");
        } else if (this.isZero()) {
            return FloatingLong.ZERO;
        } else if (toDivide.decimal == 0) {
            //If we are dividing by a whole number, use our more optimized division algorithm
            return divideEquals(toDivide.value);
        }
        BigDecimal divide = new BigDecimal(toString()).divide(new BigDecimal(toDivide.toString()), DECIMAL_DIGITS, RoundingMode.HALF_UP);
        long value = divide.longValue();
        short decimal = parseDecimal(divide.toPlainString());
        return setAndClampValues(value, decimal);
    }

    /**
     * Divides this {@link FloatingLong} by the given unsigned long primitive, modifying the current object unless it is a constant in which case it instead returns the
     * result in a new object. Rounds to the nearest 0.0001
     *
     * @param toDivide The value to divide by represented as an unsigned long.
     *
     * @return The {@link FloatingLong} representing the value of dividing this {@link FloatingLong} by the given unsigned long.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}
     * <br>
     * {@code value = value.divideEquals(toDivide)}
     */
    public FloatingLong divideEquals(long toDivide) {
        if (toDivide == 0) {
            throw new ArithmeticException("Division by zero");
        } else if (this.isZero()) {
            return FloatingLong.ZERO;
        }
        long val = Long.divideUnsigned(this.value, toDivide);
        long rem = Long.remainderUnsigned(this.value, toDivide);

        //just need to figure out remainder -> decimal
        long dec;

        //okay, now what if rem * SINGLE_UNIT * 10L will overflow?
        if (Long.compareUnsigned(rem, MAX_LONG_SHIFT / 10) >= 0) {
            //if that'll overflow, then toDivide also has to be big. let's just lose some denominator precision and use that
            dec = Long.divideUnsigned(rem, Long.divideUnsigned(toDivide, SINGLE_UNIT * 10L)); //same as multiplying numerator
        } else {
            dec = Long.divideUnsigned(rem * SINGLE_UNIT * 10L, toDivide); //trivial case
            dec += Long.divideUnsigned(this.decimal * 10L, toDivide); //need to account for dividing decimal too in case toDivide < 10k
        }

        //usually will expect to round to nearest, so we have to do that here
        if (Long.remainderUnsigned(dec, 10) >= 5) {
            dec += 10;
            if (dec >= SINGLE_UNIT * 10) { //round up + carry over to val
                val++;
                dec -= SINGLE_UNIT * 10;
            }
        }
        dec /= 10;
        return setAndClampValues(val, (short) dec);
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong} rounded down to an unsigned long.
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return An unsigned long representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    public long divideToUnsignedLong(FloatingLong toDivide) {
        if (toDivide.isZero()) {
            throw new ArithmeticException("Division by zero");
        } else if (this.smallerThan(toDivide)) {
            // Return early if operation will return < 1
            return 0;
        }
        if (toDivide.greaterOrEqual(ONE)) {
            //If toDivide >=1, then we don't care about this.decimal, so can optimize out accounting for that
            if (Long.compareUnsigned(toDivide.value, MAX_LONG_SHIFT) <= 0) { //don't case if *this* is < or > than shift
                long div = toDivide.value * SINGLE_UNIT + toDivide.decimal;
                return (Long.divideUnsigned(this.value, div) * SINGLE_UNIT) + Long.divideUnsigned(Long.remainderUnsigned(this.value, div) * SINGLE_UNIT, div);
            }
            // we already know toDivide is > max_long_shift, and other case is impossible
            if (Long.compareUnsigned(toDivide.value, Long.divideUnsigned(-1L, 2) + 1L) >= 0) {
                //need to check anyways to avoid overflow on toDivide.value +1, so might as well return early
                return 1;
            }
            long q = Long.divideUnsigned(this.value, toDivide.value);
            if (q != Long.divideUnsigned(this.value, toDivide.value + 1)) {
                // check if we need to account for toDivide.decimal in this case
                if (toDivide.value * q + Long.divideUnsigned(toDivide.decimal * q, MAX_DECIMAL) > this.value) {
                    // if we do, reduce the result by one to account for it
                    return q - 1;
                }
            }
            return q;
        }
        //In this case, we're really multiplying (definitely need to account for decimal as well)
        if (Long.compareUnsigned(this.value, MAX_LONG_SHIFT) >= 0) {
            return Long.divideUnsigned(this.value, toDivide.decimal) * MAX_DECIMAL //lose some precision here, have to add modulus
                   + Long.divideUnsigned(Long.remainderUnsigned(this.value, toDivide.decimal) * MAX_DECIMAL, toDivide.decimal)
                   + (long) this.decimal * MAX_DECIMAL / toDivide.decimal;
        }
        long d = this.value * MAX_DECIMAL;
        //Note: We don't care about modulus since we're returning integers
        return Long.divideUnsigned(d, toDivide.decimal) + ((long) this.decimal * MAX_DECIMAL / toDivide.decimal);
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong} rounded down to a signed long. This gets clamped at the upper bound of {@link
     * Long#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return A long representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    public long divideToLong(FloatingLong toDivide) {
        return MathUtils.clampUnsignedToLong(divideToUnsignedLong(toDivide));
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong} rounded down to an integer value. This gets clamped at the upper bound of {@link
     * Integer#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return An int representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    public int divideToInt(FloatingLong toDivide) {
        return MathUtils.clampUnsignedToInt(divideToLong(toDivide));
    }

    /**
     * Adds the given {@link FloatingLong} to this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The {@link FloatingLong} to add.
     *
     * @return The {@link FloatingLong} representing the value of adding the given {@link FloatingLong} to this {@link FloatingLong}.
     */
    public FloatingLong add(FloatingLong toAdd) {
        return copy().plusEqual(toAdd);
    }

    /**
     * Helper method to add an unsigned long primitive to this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The value to add represented as an unsigned long.
     *
     * @return The {@link FloatingLong} representing the value of adding the given unsigned long to this {@link FloatingLong}.
     */
    public FloatingLong add(long toAdd) {
        return add(FloatingLong.create(toAdd));
    }

    /**
     * Helper method to add a double primitive to this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toAdd The value to add, must be greater than or equal to zero.
     *
     * @return The {@link FloatingLong} representing the value of adding the given double to this {@link FloatingLong}.
     *
     * @throws IllegalArgumentException if {@code toAdd} is negative.
     */
    public FloatingLong add(double toAdd) {
        if (toAdd < 0) {
            throw new IllegalArgumentException("Addition called with negative number, this is not supported. FloatingLongs are always positive.");
        }
        return add(FloatingLong.create(toAdd));
    }

    /**
     * Subtracts the given {@link FloatingLong} from this {@link FloatingLong} and returns the result in a new object. This gets clamped at the lower bound of {@link
     * FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The {@link FloatingLong} to subtract.
     *
     * @return The {@link FloatingLong} representing the value of subtracting the given {@link FloatingLong} from this {@link FloatingLong}.
     */
    public FloatingLong subtract(FloatingLong toSubtract) {
        return copy().minusEqual(toSubtract);
    }

    /**
     * Helper method to subtract an unsigned long primitive from this {@link FloatingLong} and returns the result in a new object. This gets clamped at the lower bound of
     * {@link FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The value to subtract represented as an unsigned long.
     *
     * @return The {@link FloatingLong} representing the value of subtracting the given unsigned long from this {@link FloatingLong}.
     */
    public FloatingLong subtract(long toSubtract) {
        return subtract(FloatingLong.create(toSubtract));
    }

    /**
     * Helper method to subtract a double primitive from this {@link FloatingLong} and returns the result in a new object. This gets clamped at the lower bound of {@link
     * FloatingLong#ZERO} rather than becoming negative.
     *
     * @param toSubtract The value to subtract, must be greater than or equal to zero.
     *
     * @return The {@link FloatingLong} representing the value of subtracting the given double from this {@link FloatingLong}.
     *
     * @throws IllegalArgumentException if {@code toSubtract} is negative.
     */
    public FloatingLong subtract(double toSubtract) {
        if (toSubtract < 0) {
            throw new IllegalArgumentException("Subtraction called with negative number, this is not supported. FloatingLongs are always positive.");
        }
        return subtract(FloatingLong.create(toSubtract));
    }

    /**
     * Multiplies the given {@link FloatingLong} with this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The {@link FloatingLong} to multiply by.
     *
     * @return The {@link FloatingLong} representing the value of multiplying the given {@link FloatingLong} with this {@link FloatingLong}.
     */
    public FloatingLong multiply(FloatingLong toMultiply) {
        return copy().timesEqual(toMultiply);
    }

    /**
     * Helper method to multiple an unsigned long primitive with this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of
     * {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The value to multiply by represented as an unsigned long.
     *
     * @return The {@link FloatingLong} representing the value of multiplying the given unsigned long with this {@link FloatingLong}.
     */
    public FloatingLong multiply(long toMultiply) {
        return multiply(FloatingLong.create(toMultiply));
    }

    /**
     * Helper method to multiple a double primitive with this {@link FloatingLong} and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toMultiply The value to multiply by, must be greater than or equal to zero.
     *
     * @return The {@link FloatingLong} representing the value of multiplying the given double with this {@link FloatingLong}.
     *
     * @throws IllegalArgumentException if {@code toMultiply} is negative.
     */
    public FloatingLong multiply(double toMultiply) {
        if (toMultiply < 0) {
            throw new IllegalArgumentException("Multiply called with negative number, this is not supported. FloatingLongs are always positive.");
        }
        return multiply(FloatingLong.createConst(toMultiply));
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
    public FloatingLong divide(FloatingLong toDivide) {
        return copy().divideEquals(toDivide);
    }

    /**
     * Helper method to divide this {@link FloatingLong} by an unsigned long primitive and returns the result in a new object. This gets clamped at the upper bound of
     * {@link FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The value} to divide by represented as an unsigned long. Must not be zero
     *
     * @return The {@link FloatingLong} representing the value of dividing this {@link FloatingLong} by the given unsigned long.
     *
     * @throws ArithmeticException if {@code toDivide} is zero.
     */
    public FloatingLong divide(long toDivide) {
        return copy().divideEquals(toDivide);
    }

    /**
     * Helper method to divide this {@link FloatingLong} by a double primitive and returns the result in a new object. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing.
     *
     * @param toDivide The value} to divide by, must be greater than zero.
     *
     * @return The {@link FloatingLong} representing the value of dividing this {@link FloatingLong} by the given double.
     *
     * @throws ArithmeticException      if {@code toDivide} is zero.
     * @throws IllegalArgumentException if {@code toDivide} is negative.
     */
    public FloatingLong divide(double toDivide) {
        if (toDivide < 0) {
            throw new IllegalArgumentException("Division called with negative number, this is not supported. FloatingLongs are always positive.");
        }
        return divide(FloatingLong.create(toDivide));
    }

    /**
     * Divides this {@link FloatingLong} by the given {@link FloatingLong} and returns the result as a double. This gets clamped at the upper bound of {@link
     * FloatingLong#MAX_VALUE} rather than overflowing. Additionally if the value to divide by is zero, this returns {@code 1}
     *
     * @param toDivide The {@link FloatingLong} to divide by.
     *
     * @return A double representing the value of dividing this {@link FloatingLong} by the given {@link FloatingLong}, or {@code 1} if the given {@link FloatingLong} is
     * {@code 0}.
     *
     * @implNote This caps the returned value at {@code 1}
     */
    public double divideToLevel(FloatingLong toDivide) {
        //TODO: Optimize out creating another object
        return toDivide.isZero() || greaterThan(toDivide) ? 1 : divide(toDivide).doubleValue();
    }

    /**
     * @param other The {@link FloatingLong} to compare to
     *
     * @return this {@link FloatingLong} if it is greater than equal to the given {@link FloatingLong}, otherwise returns the given {@link FloatingLong}
     *
     * @implNote This method does not copy the value that is returned, so it is on the caller to keep track of mutability.
     */
    public FloatingLong max(FloatingLong other) {
        return smallerThan(other) ? other : this;
    }

    /**
     * @param other The {@link FloatingLong} to compare to
     *
     * @return this {@link FloatingLong} if it is smaller than equal to the given {@link FloatingLong}, otherwise returns the given {@link FloatingLong}
     *
     * @implNote This method does not copy the value that is returned, so it is on the caller to keep track of mutability.
     */
    public FloatingLong min(FloatingLong other) {
        return greaterThan(other) ? other : this;
    }

    /**
     * Returns the smallest {@link FloatingLong} that is greater than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @return the smallest {@link FloatingLong} that is greater than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @implNote If this {@link FloatingLong} is already equal to a mathematical unsigned long, then the result is the same as the argument. Additionally, if this {@link
     * FloatingLong} is larger than the maximum unsigned long, this instead returns a {@link FloatingLong} representing the maximum unsigned long.
     */
    public FloatingLong ceil() {
        if (decimal == 0) {
            return this;
        }
        if (value == -1) {
            //It is the max long value already then actually just floor it
            return new FloatingLong(value, (short) 0, false);
        }
        return new FloatingLong(value + 1, (short) 0, false);
    }

    /**
     * Returns the largest {@link FloatingLong} that is less than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @return the largest {@link FloatingLong} that is less than or equal to this {@link FloatingLong}, and is equal to a mathematical unsigned long.
     *
     * @implNote If this {@link FloatingLong} is already equal to a mathematical unsigned long, then the result is the same as the argument.
     */
    public FloatingLong floor() {
        return decimal == 0 ? this : new FloatingLong(value, (short) 0, false);
    }

    /**
     * Helper method to check if a given {@link FloatingLong} is smaller than this {@link FloatingLong}
     *
     * @param toCompare The {@link FloatingLong} to compare to
     *
     * @return {@code true} if this {@link FloatingLong} is smaller, {@code false} otherwise.
     */
    public boolean smallerThan(FloatingLong toCompare) {
        return compareTo(toCompare) < 0;
    }

    /**
     * Helper method to check if a given {@link FloatingLong} is smaller than or equal to this {@link FloatingLong}
     *
     * @param toCompare The {@link FloatingLong} to compare to
     *
     * @return {@code true} if this {@link FloatingLong} is smaller or equal, {@code false} otherwise.
     */
    public boolean smallerOrEqual(FloatingLong toCompare) {
        return compareTo(toCompare) <= 0;
    }

    /**
     * Helper method to check if a given {@link FloatingLong} is greater than this {@link FloatingLong}
     *
     * @param toCompare The {@link FloatingLong} to compare to
     *
     * @return {@code true} if this {@link FloatingLong} is larger, {@code false} otherwise.
     */
    public boolean greaterThan(FloatingLong toCompare) {
        return compareTo(toCompare) > 0;
    }

    /**
     * Helper method to check if a given {@link FloatingLong} is greater than or equal to this {@link FloatingLong}
     *
     * @param toCompare The {@link FloatingLong} to compare to
     *
     * @return {@code true} if this {@link FloatingLong} is larger or equal, {@code false} otherwise.
     */
    public boolean greaterOrEqual(FloatingLong toCompare) {
        return compareTo(toCompare) >= 0;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote zero if equal to toCompare
     * <br>
     * less than zero if smaller than toCompare
     * <br>
     * greater than zero if bigger than toCompare
     * @implNote {@code 1} or {@code -1} if the overall value is different
     * <br>
     * {@code 2} or {@code -2} if the value is the same but the decimal is different
     */
    @Override
    public int compareTo(FloatingLong toCompare) {
        int valueCompare = Long.compareUnsigned(value, toCompare.value);
        if (valueCompare == 0) {
            //Primary value is equal, check the decimal
            if (decimal < toCompare.decimal) {
                //If our primary value is equal, but our decimal smaller than toCompare's we are less than
                return -2;
            } else if (decimal > toCompare.decimal) {
                //If our primary value is equal, but our decimal bigger than toCompare's we are greater than
                return 2;
            }
            //Else we are equal
            return 0;
        }
        return valueCompare;
    }

    /**
     * Specialization of {@link #equals(Object)} for comparing two {@link FloatingLong}s
     *
     * @param other The {@link FloatingLong} to compare to
     *
     * @return {@code true} if this {@link FloatingLong} is equal in value to the given {@link FloatingLong}, {@code false} otherwise.
     */
    public boolean equals(FloatingLong other) {
        return value == other.value && decimal == other.decimal;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof FloatingLong && equals((FloatingLong) other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, decimal);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We clamp the value to MAX_INT rather than having it overflow into the negatives.
     */
    @Override
    public int intValue() {
        return MathUtils.clampUnsignedToInt(value);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We clamp the value to MAX_LONG rather than having it overflow into the negatives and being unsigned.
     */
    @Override
    public long longValue() {
        return MathUtils.clampUnsignedToLong(value);
    }

    /**
     * {@inheritDoc}
     *
     * Converts the unsigned long portion to a float in the same way Guava's UnsignedLong does, and then adds our decimal portion
     */
    @Override
    public float floatValue() {
        return MathUtils.unsignedLongToFloat(value) + decimal / (float) SINGLE_UNIT;
    }

    /**
     * {@inheritDoc}
     *
     * Converts the unsigned long portion to a double in the same way Guava's UnsignedLong does, and then adds our decimal portion
     */
    @Override
    public double doubleValue() {
        return MathUtils.unsignedLongToDouble(value) + decimal / (double) SINGLE_UNIT;
    }

    /**
     * Returns the absolute value of the difference between two Floating Long values.
     *
     * @param other comparing FloatingLong
     *
     * @return the difference between values
     */
    public FloatingLong absDifference(FloatingLong other) {
        if (greaterThan(other)) {
            return subtract(other);
        }
        return add(other);
    }

    /**
     * Writes this {@link FloatingLong} to the given buffer
     *
     * @param buffer The {@link PacketBuffer} to write to.
     */
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeVarLong(value);
        buffer.writeShort(decimal);
    }

    @Override
    public String toString() {
        return toString(DECIMAL_DIGITS);
    }

    /**
     * Extension of {@link #toString()} that allows for specifying how many decimals digits to show. If the decimal is zero, this is ignored, and this value is capped by
     * the maximum number of decimal digits
     *
     * @param decimalPlaces The number of decimal digits to display
     */
    public String toString(int decimalPlaces) {
        if (decimal == 0) {
            return Long.toUnsignedString(value);
        }
        if (decimalPlaces > DECIMAL_DIGITS) {
            decimalPlaces = DECIMAL_DIGITS;
        }
        String valueAsString = Long.toUnsignedString(value) + ".";
        String decimalAsString = Short.toString(decimal);
        int numberDigits = decimalAsString.length();
        if (numberDigits < DECIMAL_DIGITS) {
            //We need to prepend some zeros so that 1 -> 0.0001 rather than 0.01 for when we want two decimal places
            decimalAsString = getZeros(DECIMAL_DIGITS - numberDigits) + decimalAsString;
            numberDigits = DECIMAL_DIGITS;
        }
        if (numberDigits > decimalPlaces) {
            //We need to trim it
            decimalAsString = decimalAsString.substring(0, decimalPlaces);
        }
        return valueAsString + decimalAsString;
    }

    /**
     * Parses the string argument as a signed decimal {@link FloatingLong}. The characters in the string must all be decimal digits, with a decimal point being valid to
     * convey where the decimal starts.
     *
     * @param string a {@code String} containing the {@link FloatingLong} representation to be parsed
     *
     * @return the {@link FloatingLong} represented by the argument in decimal.
     *
     * @throws NumberFormatException if the string does not contain a parsable {@link FloatingLong}.
     */
    public static FloatingLong parseFloatingLong(String string) {
        return parseFloatingLong(string, false);
    }

    /**
     * Parses the string argument as a signed decimal {@link FloatingLong}. The characters in the string must all be decimal digits, with a decimal point being valid to
     * convey where the decimal starts.
     *
     * @param string     a {@code String} containing the {@link FloatingLong} representation to be parsed
     * @param isConstant Specifies if a constant floating long should be returned or a modifiable floating long
     *
     * @return the {@link FloatingLong} represented by the argument in decimal.
     *
     * @throws NumberFormatException if the string does not contain a parsable {@link FloatingLong}.
     */
    public static FloatingLong parseFloatingLong(String string, boolean isConstant) {
        long value;
        int index = string.indexOf(".");
        if (index == -1) {
            value = Long.parseUnsignedLong(string);
        } else {
            value = Long.parseUnsignedLong(string.substring(0, index));
        }
        short decimal = parseDecimal(string, index);
        return isConstant ? createConst(value, decimal) : create(value, decimal);
    }

    /**
     * Parses the decimal out of a string argument and gets the representation as a short. The characters in the string must all be decimal digits, with a decimal point
     * being valid to convey where the decimal starts.
     *
     * @param string a {@code String} containing the decimal to be parsed
     *
     * @return the decimal represented as a short.
     *
     * @throws NumberFormatException if the string does not contain a parsable {@link Short}.
     */
    private static short parseDecimal(String string) {
        return parseDecimal(string, string.indexOf("."));
    }

    /**
     * Parses the decimal out of a string argument and gets the representation as a short. The characters in the string must all be decimal digits, the given index is
     * treated as the location of the decimal.
     *
     * @param string a {@code String} containing the decimal to be parsed
     * @param index  The index of the decimal
     *
     * @return the decimal represented as a short.
     *
     * @throws NumberFormatException if the string does not contain a parsable {@link Short}.
     */
    private static short parseDecimal(String string, int index) {
        if (index == -1) {
            return 0;
        }
        String decimalAsString = string.substring(index + 1);
        int numberDigits = decimalAsString.length();
        if (numberDigits < DECIMAL_DIGITS) {
            //We need to pad it on the right with zeros
            decimalAsString += getZeros(DECIMAL_DIGITS - numberDigits);
        } else if (numberDigits > DECIMAL_DIGITS) {
            //We need to trim it to make sure it will be in range of a short
            decimalAsString = decimalAsString.substring(0, DECIMAL_DIGITS);
        }
        return Short.parseShort(decimalAsString);
    }

    /**
     * Helper method to get a given number of repeating zeros as a String
     *
     * @param number The number of zeros to put in the string.
     */
    private static String getZeros(int number) {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i < number; i++) {
            zeros.append('0');
        }
        return zeros.toString();
    }

    /**
     * Internal helper to determine if the result of unsigned long multiplication will overflow.
     */
    private static boolean multiplyLongsWillOverFlow(long a, long b) {
        return (a != 0 && b != 0 && Long.compareUnsigned(b, Long.divideUnsigned(-1, a)) > 0);
    }

    /**
     * Internal helper to multiply two longs and clamp if they overflow.
     */
    private static long multiplyLongs(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        } else if (multiplyLongsWillOverFlow(a, b)) {
            return -1;
        }
        return a * b;
    }

    /**
     * Internal helper to multiply a long by a decimal.
     */
    private static FloatingLong multiplyLongAndDecimal(long value, short decimal) {
        //This can't overflow!
        if (Long.compareUnsigned(value, Long.divideUnsigned(-1, SINGLE_UNIT)) > 0) {
            return create(Long.divideUnsigned(value, SINGLE_UNIT) * decimal, (short) (value % SINGLE_UNIT * decimal));
        }
        return create(Long.divideUnsigned(value * decimal, SINGLE_UNIT), (short) (value * decimal % SINGLE_UNIT));
    }

    /**
     * Internal helper to multiply two decimals.
     */
    private static FloatingLong multiplyDecimals(short a, short b) {
        //Note: If we instead wanted to round here, just get modulus and add if >= 0.5*SINGLE_UNIT
        long temp = (long) a * (long) b / SINGLE_UNIT;
        return create(0, (short) temp);
    }
}