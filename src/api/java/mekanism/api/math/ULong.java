package mekanism.api.math;

public class ULong {
    /**
     * Clamp an unsigned long to int
     *
     * @param l unsigned long to clamp
     *
     * @return an int clamped to {@link Integer#MAX_VALUE}
     */
    public static int clampToInt(long l) {
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
    public static long clampToSigned(long l) {
        return l < 0 ? Long.MAX_VALUE : l;
    }

    /**
     * Converts an unsigned long to a double, using the same math as in Guava's UnsignedLong class
     */
    public static float toFloat(long l) {
        float fValue = (float) (l & MathUtils.UNSIGNED_MASK);
        if (l < 0) {
            fValue += 0x1.0p63F;
        }
        return fValue;
    }

    /**
     * Converts an unsigned long to a double, using the same math as in Guava's UnsignedLong class
     */
    public static double toDouble(long l) {
        double dValue = (double) (l & MathUtils.UNSIGNED_MASK);
        if (l < 0) {
            dValue += 0x1.0p63;
        }
        return dValue;
    }

}
