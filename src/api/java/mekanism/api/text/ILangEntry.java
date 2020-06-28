package mekanism.api.text;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Helper interface for creating formatted translations in our lang enums
 */
public interface ILangEntry extends IHasTranslationKey {

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place
     */
    default TranslationTextComponent translate(Object... args) {
        return TextComponentUtil.smartTranslate(getTranslationKey(), args);
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.util.text.Color} of the given {@link EnumColor} to the {@link ITextComponent}
     */
    default IFormattableTextComponent translateColored(EnumColor color, Object... args) {
        return TextComponentUtil.build(color, translate(args));
    }
}