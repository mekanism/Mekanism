package mekanism.tools.common.config;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ToolsConfig implements IMekanismConfig {

    private final ForgeConfigSpec configSpec;

    ToolsConfig() {
        ForgeConfigSpec.Builder builder  = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Tools Config");



        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "mekanism-tools.toml";
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