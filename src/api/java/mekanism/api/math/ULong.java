package mekanism.api.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

@SuppressWarnings("unused")
public class ULong {

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
}
