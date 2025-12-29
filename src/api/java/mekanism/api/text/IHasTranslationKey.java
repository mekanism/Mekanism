package mekanism.api.text;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

@MethodsAreNotNullByDefault
public interface IHasTranslationKey {

    /**
     * Gets the translation key for this object.
     */
    String getTranslationKey();

    /**
     * Helper interface that also implements Neo's TranslatableEnum interface
     * @since 10.7.3
     */
    interface IHasEnumNameTranslationKey extends IHasTranslationKey, TranslatableEnum {

        @NotNull
        @Override
        default Component getTranslatedName() {
            return TextComponentUtil.translate(getTranslationKey());
        }
    }
}