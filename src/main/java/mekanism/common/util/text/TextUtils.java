package mekanism.common.util.text;

import mekanism.common.content.qio.QIOFrequency;

public final class TextUtils {

    private TextUtils() {
    }

    private static final String HEX_PREFIX = "0x";
    private static final char[] HEX_CODES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getPercent(double ratio) {
        return Math.round(ratio * 100) + "%";
    }

    public static String format(int types) {
        return QIOFrequency.intFormatter.format(types);
    }

    public static String format(long count) {
        return QIOFrequency.intFormatter.format(count);
    }

    public static String hex(boolean prefix, int bytes, long value) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < bytes * 2; i++) {
            ret.insert(0, HEX_CODES[(int) (value & 0xF)]);
            value >>= 4;
        }
        if (prefix) {
            ret.insert(0, HEX_PREFIX);
        }
        return ret.toString();
    }
}
