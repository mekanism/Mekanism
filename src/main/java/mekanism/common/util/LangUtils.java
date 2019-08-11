package mekanism.common.util;

import net.minecraft.util.text.translation.I18n;

public final class LangUtils {

    /**
     * Localizes the defined string.
     *
     * @param s - string to localized
     *
     * @return localized string
     */
    public static String localize(String s) {
        return I18n.translateToLocal(s);
    }
}