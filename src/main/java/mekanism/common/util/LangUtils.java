package mekanism.common.util;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;

public final class LangUtils {

    public static String transOnOff(boolean b) {
        return LangUtils.localize("mekanism.gui." + (b ? "on" : "off"));
    }

    public static String transYesNo(boolean b) {
        return LangUtils.localize("mekanism.tooltip." + (b ? "yes" : "no"));
    }

    public static String localizeFluidStack(FluidStack fluidStack) {
        return fluidStack == null ? null : fluidStack.getFluid().getLocalizedName(fluidStack);
    }

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