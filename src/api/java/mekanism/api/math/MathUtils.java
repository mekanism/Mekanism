package mekanism.api.math;

import java.util.List;

public class MathUtils {

    private MathUtils() {
    }

    static final long UNSIGNED_MASK = 0x7FFFFFFFFFFFFFFFL;

    /**
     * Clamp a double to int without using{@link Math#min(double, double)} due to double representation issues. Primary use: power systems that use int, where Mek uses
     * doubles internally
     * <p>
     * <code>
     * double d = 1e300; // way bigger than longs, so the long should always be what's returned by Math.min System.out.println((long)Math.min(123456781234567812L, d)); //
     * result is 123456781234567808 - 4 less than what you'd expect System.out.println((long)Math.min(123456789012345678L, d)); // result is 123456789012345680 - 2 more
     * than what you'd expect
     * </code>
     *
     * @param d double to clamp
     *
     * @return an int clamped to {@link Integer#MAX_VALUE}
     *
     * @see <a href="https://github.com/aidancbrady/Mekanism/pull/5203">Original PR</a>
     */
    public static int clampToInt(double d) {
        if (d < Integer.MAX_VALUE) {
            return (int) d;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Clamp a long to int
     *
     * @param l long to clamp
     *
     * @return an int clamped to {@link Integer#MAX_VALUE}
     */
    public static int clampToInt(long l) {
        if (l < Integer.MAX_VALUE) {
            return (int) l;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Clamp a double to a long
     *
     * @param d double to clamp
     *
     * @return a long clamped to {@link Long#MAX_VALUE}
     */
    public static long clampToLong(double d) {
        if (d < Long.MAX_VALUE) {
            return (long) d;
        }
        return Long.MAX_VALUE;
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
     */
    public static double divideToLevel(double numerator, double toDivide) {
        return toDivide == 0D || numerator > toDivide ? 1 : numerator / toDivide;
    }
}