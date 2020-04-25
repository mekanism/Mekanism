package mekanism.api.text;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
     * Translates this {@link ILangEntry} and applies the {@link TextFormatting} of the given {@link EnumColor} to the {@link ITextComponent}
     */
    default ITextComponent translateColored(EnumColor color, Object... args) {
        return translateFormatted(color.textFormatting, args);
    }

    /**
     * Translates this {@link ILangEntry} and applies the given {@link TextFormatting} to the {@link ITextComponent}
     */
    default ITextComponent translateFormatted(TextFormatting format, Object... args) {
        return translate(args).applyTextStyle(format);
    }
}