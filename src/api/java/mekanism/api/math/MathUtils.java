package mekanism.api.math;

import java.util.List;

public class MathUtils {

    private MathUtils() {
    }

    /**
     * Clamp a double to int without using{@link Math#min(double, double)} due to double representation issues. Primary use: power systems that use int, where Mek uses
     * doubles internally
     * <pre>{@code
     * // way bigger than longs, so the long should always be what's returned by Math.min
     * double d = 1e300;
     * // result is 123456781234567808 - 4 less than what you'd expect
     * System.out.println((long)Math.min(123456781234567812L, d));
     * // result is 123456789012345680 - 2 more than what you'd expect
     * System.out.println((long)Math.min(123456789012345678L, d));
     * }</pre>
     *
     * @param d double to clamp
     *
     * @return an int clamped to {@link Integer#MAX_VALUE}
     *
     * @see <a href="https://github.com/aidancbrady/Mekanism/pull/5203">Original PR</a>
     * @see com.google.common.primitives.Ints#saturatedCast(long)
     */
    public static int clampToInt(double d) {
        if (d > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (d < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) d;
    }

    /**
     * Clamp a double to a long
     *
     * @param d double to clamp
     *
     * @return a long clamped to {@link Long#MAX_VALUE}
     *
     * @see com.google.common.primitives.Ints#saturatedCast(long)
     */
    public static long clampToLong(double d) {
        if (d > Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        } else if (d < Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return (long) d;
    }

    /**
     * Gets an element in an array by index, taking the mod (or floored mod if negative).
     *
     * @param elements Elements.
     * @param index    Index.
     *
     * @return Element at the given index.
     */
    public static <TYPE> TYPE getByIndexMod(TYPE[] elements, int index) {
        if (index < 0) {
            return elements[Math.floorMod(index, elements.length)];
        }
        return elements[index % elements.length];
    }

    /**
     * Gets an element in a list by index, taking the mod (or floored mod if negative).
     *
     * @param elements Elements.
     * @param index    Index.
     *
     * @return Element at the given index.
     */
    public static <TYPE> TYPE getByIndexMod(List<TYPE> elements, int index) {
        if (index < 0) {
            return elements.get(Math.floorMod(index, elements.size()));
        }
        return elements.get(index % elements.size());
    }

    /**
     * Divides numerator by the given toDivide and returns the result as a double. Additionally, if the value to divide by is zero, this returns {@code 1}
     *
     * @param numerator The numerator of the division.
     * @param toDivide  The denominator of the division.
     *
     * @return A double representing the value of dividing numerator by toDivide, or {@code 1} if the given toDivide is {@code 0}.
     *
     * @implNote This caps the returned value at {@code 1}
     * @since 10.6.6
     */
    public static double divideToLevel(double numerator, double toDivide) {
        return toDivide == 0D || numerator >= toDivide ? 1 : numerator / toDivide;
    }

    /**
     * Like {@link Math#addExact(long, long)} but clamps to max long instead of throwing
     *
     * @param x the first value
     * @param y the second value
     *
     * @return the result or max long if it overflows
     *
     * @since 10.6.6
     */
    public static long addClamped(long x, long y) {
        long r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            return Long.MAX_VALUE;
        }
        return r;
    }

    /**
     * Like {@link Math#multiplyExact(long, long)} but clamps to max long instead of throwing
     *
     * @param x the first value. should be positive
     * @param y the second value. should be positive
     *
     * @return the result or max long if it overflows
     *
     * @since 10.6.6
     */
    public static long multiplyClamped(long x, long y) {
        //TODO: Re-evaluate usages of this and addClamped, and try to make it so that we don't have it possible for things to overflow instead
        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if ((ax | ay) >>> 31 != 0) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if (((y != 0) && (r / y != x)) ||
                (x == Long.MIN_VALUE && y == -1)) {
                return Long.MAX_VALUE;
            }
        }
        return r;
    }

    /**
     * Like {@link Math#addExact(int, int)} but clamps to max int instead of throwing
     *
     * @param x the first value
     * @param y the second value
     *
     * @return the result or max int if it overflows
     *
     * @since 10.8.0
     */
    public static int addClamped(int x, int y) {
        int r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            return Integer.MAX_VALUE;
        }
        return r;
    }

    /**
     * Like {@link Math#multiplyExact(int, int)} but clamps to max int instead of throwing
     *
     * @param x the first value. should be positive
     * @param y the second value. should be positive
     *
     * @return the result or max int if it overflows
     *
     * @since 10.8.0
     */
    public static int multiplyClamped(int x, int y) {
        //TODO: Re-evaluate usages of this and addClamped, and try to make it so that we don't have it possible for things to overflow instead
        long r = x * (long) y;
        if ((int) r != r) {
            return Integer.MAX_VALUE;
        }
        return (int) r;
    }
}