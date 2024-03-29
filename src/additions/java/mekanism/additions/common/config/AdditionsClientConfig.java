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
        builder.comment("Mekanism Additions Client Config. This config only exists on the client.").push("additions-client");

        voiceKeyIsToggle = CachedBooleanValue.wrap(this, builder.comment("If the voice server is enabled and voiceKeyIsToggle is also enabled, the voice key will "
                                                                         + "act as a toggle instead of requiring to be held while talking.")
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