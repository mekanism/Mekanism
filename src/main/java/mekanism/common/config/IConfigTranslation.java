package mekanism.common.config;

import mekanism.api.text.IHasTranslationKey;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IConfigTranslation extends IHasTranslationKey {

    String translation();

    default ModConfigSpec.Builder applyToBuilder(ModConfigSpec.Builder builder) {
        return builder.translation(getTranslationKey()).comment(translation());
    }

    record ConfigTranslation(String getTranslationKey, String translation) implements IConfigTranslation {
    }
}