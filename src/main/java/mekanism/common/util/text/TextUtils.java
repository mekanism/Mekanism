package mekanism.common.util.text;

public final class TextUtils {

    public static String getPercent(double ratio) {
        return Long.toString(Math.round(ratio * 100)) + "%";
    }
}
