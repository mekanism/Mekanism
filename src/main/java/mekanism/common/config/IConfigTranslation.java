package mekanism.common.config;

import mekanism.api.text.IHasTranslationKey;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IConfigTranslation extends IHasTranslationKey {

    String title();

    String tooltip();

    default ModConfigSpec.Builder applyToBuilder(ModConfigSpec.Builder builder) {
        return builder.translation(getTranslationKey()).comment(tooltip());
    }

    record ConfigTranslation(String getTranslationKey, String title, String tooltip) implements IConfigTranslation {
    }
}