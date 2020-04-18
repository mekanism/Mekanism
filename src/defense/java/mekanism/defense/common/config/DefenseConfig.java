package mekanism.defense.common.config;

import mekanism.common.config.BaseMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class DefenseConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    DefenseConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Defense Config. This config is synced between server and client.").push("defense");

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "defense";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}