package mekanism.defense.common.config;

import mekanism.common.config.BaseMekanismConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForgeConfigSpec;

public class DefenseConfig extends BaseMekanismConfig {

    private final NeoForgeConfigSpec configSpec;

    DefenseConfig() {
        NeoForgeConfigSpec.Builder builder = new NeoForgeConfigSpec.Builder();
        builder.comment("Mekanism Defense Config. This config is synced between server and client.").push("defense");

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "defense";
    }

    @Override
    public NeoForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}