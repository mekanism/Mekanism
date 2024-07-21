package mekanism.additions.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AdditionsClientConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedBooleanValue voiceKeyIsToggle;

    AdditionsClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        AdditionsConfigTranslations.CLIENT_TOP_LEVEL.applyToBuilder(builder).push("additions-client");

        voiceKeyIsToggle = CachedBooleanValue.wrap(this, AdditionsConfigTranslations.CLIENT_VOICE_KEY_TOGGLE.applyToBuilder(builder)
              .define("voiceKeyIsToggle", false));
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "additions-client";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}