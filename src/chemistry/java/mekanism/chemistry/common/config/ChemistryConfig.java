package mekanism.chemistry.common.config;

import mekanism.common.config.BaseMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ChemistryConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    ChemistryConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Mekanism Chemistry Config. This config is synced between server and client.").push("chemistry");

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "chemistry";
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
