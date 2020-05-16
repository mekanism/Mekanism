package mekanism.common.util.text;

public final class TextUtils {

    public static String getPercent(double ratio) {
        return Math.round(ratio * 100) + "%";
    }
}
