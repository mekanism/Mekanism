package mekanism.common.util.text;

import java.text.NumberFormat;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public final class TextUtils {

    private TextUtils() {
    }

    private static final String HEX_PREFIX = "0x";
    private static final char[] HEX_CODES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();
    private static final NumberFormat doubleFormatter = NumberFormat.getNumberInstance();

    public static ITextComponent getPercent(double ratio) {
        return MekanismLang.GENERIC_PERCENT.translate(Math.round(ratio * 100));
    }

    public static String format(long count) {
        return intFormatter.format(count);
    }

    public static String format(double count) {
        return doubleFormatter.format(count);
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