package mekanism.api.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

@SuppressWarnings("unused")
public class ULong {

    public static final @Unsigned long MAX_VALUE = -1L; // Equivalent to 2^64 - 1

    public static final Codec<Long> CODEC = new PrimitiveCodec<Long>() {
        @Override
        public <T> DataResult<Long> read(DynamicOps<T> ops, T input) {
            DataResult<String> stringValue = ops.getStringValue(input);
            if (stringValue.isSuccess()) {
                return stringValue.flatMap(number -> {
                    try {
                        return DataResult.success(Long.parseUnsignedLong(number));
                    } catch (NumberFormatException e) {
                        return DataResult.error(e::getMessage);
                    }
                });
            } else {
                return ops.getNumberValue(input).map(Number::longValue);
            }
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Long value) {
            long v = value;
            if (v >= 0) {
                //no overflow
                return ops.createLong(v);
            }
            return ops.createString(Long.toUnsignedString(v));
        }
    };

    public static final Codec<Long> NONZERO_CODEC = CODEC.validate(f -> {
        if (f == 0L) {
            return DataResult.error(() -> "Value must be greater than zero");
        }
        return DataResult.success(f);
    });

    //Copied from UnsignedLong
    public static BigInteger toBigInteger(@Unsigned long value) {
        BigInteger bigInt = BigInteger.valueOf(value & MathUtils.UNSIGNED_MASK);
        if (value < 0) {
            bigInt = bigInt.setBit(Long.SIZE - 1);
        }
        return bigInt;
    }

    public static BigDecimal toBigDecimal(@Unsigned long value) {
        return new BigDecimal(toBigInteger(value));
    }

    /**
     * Performs a double division of 2 unsigned longs
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the double result of dividend/divisor
     *
     * @see Long#divideUnsigned(long, long) for integer division
     * @see Long#remainderUnsigned(long, long) for modulo
     */
    public static double divide(@Unsigned long dividend, @Unsigned long divisor) {
        if (dividend == 0 || (dividend >= 0 && divisor > 0)) {
            //within signed long range
            return (double) dividend / divisor;
        }
        return toBigDecimal(dividend).divide(toBigDecimal(divisor), MathContext.DECIMAL64).doubleValue();
    }

    /**
     * Returns the unsigned quotient of dividing the first argument by the second where each argument and the result is interpreted as an unsigned value.
     *
     * @param dividend the value to be divided
     * @param divisor  the value doing the dividing
     *
     * @return the unsigned quotient of the first argument divided by the second argument
     */
    public static long divideLong(long dividend, long divisor) {
        return Long.divideUnsigned(dividend, divisor);
    }

    /**
     * Returns the unsigned remainder from dividing the first argument by the second where each argument and the result is interpreted as an unsigned value.
     *
     * @param dividend the value to be divided
     * @param divisor  the value doing the dividing
     *
     * @return the unsigned remainder of the first argument divided by the second argument
     *
     * @see #divideLong
     */
    public static long remainder(long dividend, long divisor) {
        return Long.remainderUnsigned(dividend, divisor);
    }

    /**
     * Clamp an unsigned long to int
     *
     * @param l unsigned long to clamp
     *
     * @return an int clamped to {@link Integer#MAX_VALUE}
     */
    public static int clampToInt(@Unsigned long l) {
        if (l < 0 || l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) l;
    }

    /**
     * Clamp an unsigned long to int
     *
     * @param l unsigned long to clamp
     *
     * @return an int clamped to {@link Long#MAX_VALUE}
     */
    public static long clampToSigned(@Unsigned long l) {
        return l < 0 ? Long.MAX_VALUE : l;
    }

    /**
     * Converts an unsigned long to a double, using the same math as in Guava's UnsignedLong class
     */
    public static float toFloat(@Unsigned long l) {
        float fValue = (float) (l & MathUtils.UNSIGNED_MASK);
        if (l < 0) {
            fValue += 0x1.0p63F;
        }
        return fValue;
    }

    /**
     * Converts an unsigned long to a double, using the same math as in Guava's UnsignedLong class
     */
    public static double toDouble(@Unsigned long l) {
        double dValue = (double) (l & MathUtils.UNSIGNED_MASK);
        if (l < 0) {
            dValue += 0x1.0p63;
        }
        return dValue;
    }

    /**
     * Performs an unsigned < check
     * @param lhs left hand side
     * @param rhs right hand side
     * @return true when lhs < rhs
     */
    public static boolean lt(@Unsigned long lhs, @Unsigned long rhs) {
        return Long.compareUnsigned(lhs, rhs) < 0;
    }

    /**
     * Performs an unsigned <= check
     * @param lhs left hand side
     * @param rhs right hand side
     * @return true when lhs <= rhs
     */
    public static boolean lte(@Unsigned long lhs, @Unsigned long rhs) {
        return Long.compareUnsigned(lhs, rhs) <= 0;
    }

    /**
     * Performs an unsigned > check
     * @param lhs left hand side
     * @param rhs right hand side
     * @return true when lhs > rhs
     */
    public static boolean gt(@Unsigned long lhs, @Unsigned long rhs) {
        return Long.compareUnsigned(lhs, rhs) > 0;
    }

    /**
     * Performs an unsigned >= check
     * @param lhs left hand side
     * @param rhs right hand side
     * @return true when lhs >= rhs
     */
    public static boolean gte(@Unsigned long lhs, @Unsigned long rhs) {
        return Long.compareUnsigned(lhs, rhs) >= 0;
    }

    /**
     * Returns the smallest of two {@code @Unsigned long} values. That is, the result is the argument closer to 0.
     *
     * @param a an argument.
     * @param b another argument.
     *
     * @return the smallest of {@code a} and {@code b}.
     */
    public static @Unsigned long min(@Unsigned long a, @Unsigned long b) {
        return lte(a, b) ? a : b;
    }

    /**
     * Returns the smallest of three {@code @Unsigned long} values. That is, the result is the argument closer to 0.
     *
     * @param a an argument.
     * @param b another argument.
     * @param c another argument.
     *
     * @return the smallest of {@code a}, {@code b} and {@code c}.
     */
    public static @Unsigned long min(@Unsigned long a, @Unsigned long b, @Unsigned long c) {
        return min(min(a, b), c);
    }

    /**
     * Returns the greatest of two {@code long} values. That is, the result is the argument closer to the value of {@link #MAX_VALUE}.
     *
     * @param a an argument.
     * @param b another argument.
     *
     * @return the largest of {@code a} and {@code b}.
     */
    public static @Unsigned long max(@Unsigned long a, @Unsigned long b) {
        return gte(a, b) ? a : b;
    }

    /**
     * Returns the greatest of three {@code long} values. That is, the result is the argument closer to the value of {@link #MAX_VALUE}.
     *
     * @param a an argument.
     * @param b another argument.
     * @param c another argument.
     *
     * @return the largest of {@code a} and {@code b} and {@code c}.
     */
    public static @Unsigned long max(@Unsigned long a, @Unsigned long b, @Unsigned long c) {
        return max(max(a, b), c);
    }

    /**
     * Internal helper to determine if the result of unsigned long multiplication will overflow.
     */
    private static boolean multiplyLongsWillOverFlow(long a, long b) {
        return (a != 0 && b != 0 && Long.compareUnsigned(b, Long.divideUnsigned(-1, a)) > 0);
    }

    /**
     * Multiply two longs and clamp if they overflow.
     */
    public static long multiply(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        } else if (multiplyLongsWillOverFlow(a, b)) {
            return MAX_VALUE;
        }
        return a * b;
    }
}
