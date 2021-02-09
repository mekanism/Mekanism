package mekanism.common.util.text;

import java.text.NumberFormat;

public final class TextUtils {

    private TextUtils() {
    }

    private static final String HEX_PREFIX = "0x";
    private static final char[] HEX_CODES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();

    public static String getPercent(double ratio) {
        //TODO - 10.1: Move this over to a lang key and make this return an ITextComponent
        // That or more the percentage symbols into the lang keys that end up taking the percent as a param
        return Math.round(ratio * 100) + "%";
    }

    public static String format(long count) {
        return intFormatter.format(count);
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