package mekanism.api.text;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

public interface IHasTranslationKey {

    /// Gets the translation key for this object.
    String getTranslationKey();

    /// Helper interface that also implements Neo's TranslatableEnum interface
    ///
    /// @since 10.7.3
    interface IHasEnumNameTranslationKey extends IHasTranslationKey, TranslatableEnum {

        @Override
        default Component getTranslatedName() {
            return TextComponentUtil.translate(getTranslationKey());
        }
    }
}