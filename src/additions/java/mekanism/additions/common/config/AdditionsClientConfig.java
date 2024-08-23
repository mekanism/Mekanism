package mekanism.additions.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AdditionsClientConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedBooleanValue pushToTalk;

    AdditionsClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        pushToTalk = CachedBooleanValue.wrap(this, AdditionsConfigTranslations.CLIENT_PUSH_TO_TALK.applyToBuilder(builder)
              .define("pushToTalk", true));

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "additions-client";
    }

    @Override
    public String getTranslation() {
        return "Client Config";
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