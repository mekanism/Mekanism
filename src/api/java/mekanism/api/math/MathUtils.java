package mekanism.api.math;

public class MathUtils {

    /**
     * Clamp a double to int without using Math.min due to double representation issues. Primary use: power systems that use int, where Mek uses doubles internally
     *
     * <code>
     * double d = 1e300; // way bigger than longs, so the long should always be what's returned by Math.min System.out.println((long)Math.min(123456781234567812L, d)); //
     * result is 123456781234567808 - 4 less than what you'd expect System.out.println((long)Math.min(123456789012345678L, d)); // result is 123456789012345680 - 2 more
     * than what you'd expect
     * </code>
     *
     * @param d double to clamp
     *
     * @return an int clamped to Integer.MAX_VALUE
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
     * Clamp a double to int without using Math.min due to double representation issues. Primary use: power systems that use int, where Mek uses doubles internally
     *
     * <code>
     * double d = 1e300; // way bigger than longs, so the long should always be what's returned by Math.min System.out.println((long)Math.min(123456781234567812L, d)); //
     * result is 123456781234567808 - 4 less than what you'd expect System.out.println((long)Math.min(123456789012345678L, d)); // result is 123456789012345680 - 2 more
     * than what you'd expect
     * </code>
     *
     * @param d double to clamp
     *
     * @return an int clamped to Integer.MAX_VALUE
     *
     * @see <a href="https://github.com/aidancbrady/Mekanism/pull/5203">Original PR</a>
     */
    public static int clampToInt(long d) {
        if (d < Integer.MAX_VALUE) {
            return (int) d;
        }
        return Integer.MAX_VALUE;
    }
}