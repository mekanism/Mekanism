package mekanism.common.util.text;

import java.text.NumberFormat;
import java.util.Locale;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.Level;

public final class TextUtils {

    private TextUtils() {
    }

    private static final String HEX_PREFIX = "0x";
    private static final char[] HEX_CODES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();
    private static final NumberFormat doubleFormatter = NumberFormat.getNumberInstance();

    public static Component getPercent(double ratio) {
        return MekanismLang.GENERIC_PERCENT.translate(Math.round(ratio * 100));
    }

    public static Component getHoursMinutes(Level level, long ticks) {
        return getHoursMinutes(level.tickRateManager(), ticks);
    }

    public static Component getHoursMinutes(TickRateManager tickRateManager, long ticks) {
        //Ensure no one somehow passes in invalid data
        float tickRate = Math.max(tickRateManager.tickrate(), TickRateManager.MIN_TICKRATE);
        int minutes = Mth.ceil(ticks / (60.0 * tickRate));
        int hours = minutes / 60;
        return hours > 0 ? MekanismLang.GENERIC_HOURS_MINUTES.translate(hours, minutes % 60) : MekanismLang.GENERIC_MINUTES.translate(minutes);
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

    public static String formatAndCapitalize(String s) {
        boolean isFirst = true;
        StringBuilder ret = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '_' || c == ' ') {
                isFirst = true;
                ret.append(' ');
            } else {
                ret.append(isFirst ? Character.toString(c).toUpperCase(Locale.ROOT) : c);
                isFirst = false;
            }
        }
        return ret.toString();
    }
}