package mekanism.api.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

/// Helper interface for creating formatted translations in our lang enums
public interface ILangEntry extends IHasTranslationKey {

    /// Translates this [ILangEntry] using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
    default MutableComponent translate(@Nullable Object... args) {
        return TextComponentUtil.smartTranslate(getTranslationKey(), args);
    }

    /// Translates this [ILangEntry] using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
    default MutableComponent translate() {
        return TextComponentUtil.translate(getTranslationKey());
    }

    /// Translates this [ILangEntry] and applies the [net.minecraft.network.chat.TextColor] of the given [EnumColor] to the [Component].
    default MutableComponent translateColored(EnumColor color, @Nullable Object... args) {
        return translateColored(color.getColor(), args);
    }

    /// Translates this [ILangEntry] and applies the [net.minecraft.network.chat.TextColor] of the given [EnumColor] to the [Component].
    default MutableComponent translateColored(EnumColor color) {
        return translateColored(color.getColor());
    }

    /// Translates this [ILangEntry] and applies the [net.minecraft.network.chat.TextColor] to the [Component].
    ///
    /// @since 10.4.0
    default MutableComponent translateColored(TextColor color, @Nullable Object... args) {
        return TextComponentUtil.build(color, translate(args));
    }

    /// Translates this [ILangEntry] and applies the [net.minecraft.network.chat.TextColor] to the [Component].
    ///
    /// @since 10.4.0
    default MutableComponent translateColored(TextColor color) {
        return TextComponentUtil.build(color, translate());
    }
}