package mekanism.common.base;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Helper interface for creating formatted translations in our lang enums
 */
public interface ILangEntry extends IHasTranslationKey {

    //TODO: JavaDoc all these methods and when to use them
    default TranslationTextComponent translate(Object... args) {
        return TextComponentUtil.smartTranslate(getTranslationKey(), args);
    }

    default ITextComponent translateColored(EnumColor color, Object... args) {
        return translateFormatted(color.textFormatting, args);
    }

    default ITextComponent translateFormatted(TextFormatting format, Object... args) {
        return translate(args).applyTextStyle(format);
    }
}