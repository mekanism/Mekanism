package mekanism.common.util;

import net.minecraft.util.RandomSource;

public class StatUtils {

    private StatUtils() {
    }

    private static final RandomSource rand = RandomSource.create();
    private static final double STIRLING_COEFF = 1 / Math.sqrt(2 * Math.PI);

    //TODO: Re-evaluate the need for this
    public static int inversePoisson(double mean) {
        double r = rand.nextDouble() * Math.exp(mean);
        int m = 0;
        double p = 1;
        double stirlingValue = mean * Math.E;
        double mBound = 3 * Math.ceil(mean);
        while ((p < r) && (m < mBound)) {
            m++;
            p += STIRLING_COEFF / Math.sqrt(m) * Math.pow(stirlingValue / m, m);
        }
        return m;
    }

    public static double min(double... vals) {
        double min = vals[0];
        for (int i = 1; i < vals.length; i++) {
            min = Math.min(min, vals[i]);
        }
        return min;
    }

    public static double max(double... vals) {
        double max = vals[0];
        for (int i = 1; i < vals.length; i++) {
            max = Math.max(max, vals[i]);
        }
        return max;
    }

    public static float wrapDegrees(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
}