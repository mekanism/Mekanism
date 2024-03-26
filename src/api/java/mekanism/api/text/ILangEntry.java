package mekanism.api.text;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/**
 * Helper interface for creating formatted translations in our lang enums
 */
@MethodsReturnNonnullByDefault
public interface ILangEntry extends IHasTranslationKey {

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
     */
    default MutableComponent translate(Object... args) {
        return TextComponentUtil.smartTranslate(getTranslationKey(), args);
    }

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
     */
    default MutableComponent translate() {
        return TextComponentUtil.translate(getTranslationKey());
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} of the given {@link EnumColor} to the {@link Component}.
     */
    default MutableComponent translateColored(EnumColor color, Object... args) {
        return translateColored(color.getColor(), args);
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} of the given {@link EnumColor} to the {@link Component}.
     */
    default MutableComponent translateColored(EnumColor color) {
        return translateColored(color.getColor());
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} to the {@link Component}.
     *
     * @since 10.4.0
     */
    default MutableComponent translateColored(TextColor color, Object... args) {
        return TextComponentUtil.build(color, translate(args));
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} to the {@link Component}.
     *
     * @since 10.4.0
     */
    default MutableComponent translateColored(TextColor color) {
        return TextComponentUtil.build(color, translate());
    }
}