package mekanism.common.util;

import java.util.Random;

public class StatUtils {

    public static Random rand = new Random();

    public static int inversePoisson(double mean) {
        double r = rand.nextDouble() * Math.exp(mean);
        int m = 0;
        double p = 1;
        double stirlingValue = mean * Math.E;
        double stirlingCoeff = 1 / Math.sqrt(2 * Math.PI);
        while ((p < r) && (m < 3 * Math.ceil(mean))) {
            m++;
            p += stirlingCoeff / Math.sqrt(m) * Math.pow(stirlingValue / m, m);
        }
        return m;
    }
}