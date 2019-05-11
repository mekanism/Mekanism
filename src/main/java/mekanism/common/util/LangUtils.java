package mekanism.common.util;

import java.util.IllegalFormatException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;

public final class LangUtils {

    public static String transOnOff(boolean b) {
        return LangUtils.localize(transOnOffKey(b));
    }

    public static String transOnOffKey(boolean b) {
        return "gui." + (b ? "on" : "off");
    }

    public static String transYesNo(boolean b) {
        return LangUtils.localize("tooltip." + (b ? "yes" : "no"));
    }

    public static String transOutputInput(boolean b) {
        return LangUtils.localize("gui." + (b ? "output" : "input"));
    }

    public static String localizeFluidStack(FluidStack fluidStack) {
        return (fluidStack == null || fluidStack.getFluid() == null) ? null
                                                                     : fluidStack.getFluid().getLocalizedName(fluidStack);
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

    public static String localizeWithFormat(String key, Object... format) {
        String s = localize(key);

        try {
            return String.format(s, format);
        } catch (IllegalFormatException e) {
            return "Format error: " + s;
        }
    }

    public static TextComponentTranslation translationWithColour(String langKey, TextFormatting color) {
        TextComponentTranslation translation = new TextComponentTranslation(langKey);
        translation.getStyle().setColor(color);
        return translation;
    }

    public static <T extends ITextComponent> T withColor(T component, TextFormatting color) {
        component.getStyle().setColor(color);
        return component;
    }

    public static TextComponentTranslation onOffColoured(boolean isOn) {
        TextComponentTranslation translation = new TextComponentTranslation(transOnOffKey(isOn));
        translation.getStyle().setColor(isOn ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
        return translation;
    }
}
