package mekanism.generators.common.config;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class GeneratorsConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    GeneratorsConfig() {
        ForgeConfigSpec.Builder builder  = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Generators Config");



        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-generators.toml";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.COMMON;
    }
}