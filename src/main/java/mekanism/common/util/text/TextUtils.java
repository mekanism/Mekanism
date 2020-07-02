package mekanism.common.util.text;

import mekanism.common.content.qio.QIOFrequency;

public final class TextUtils {

    public static String getPercent(double ratio) {
        return Math.round(ratio * 100) + "%";
    }

    public static String format(int types) {
        return QIOFrequency.intFormatter.format(types);
    }

    public static String format(long count) {
        return QIOFrequency.intFormatter.format(count);
    }
}
