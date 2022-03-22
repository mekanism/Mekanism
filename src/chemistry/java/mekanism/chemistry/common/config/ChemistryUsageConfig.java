package mekanism.chemistry.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ChemistryUsageConfig extends BaseMekanismConfig {

    public final CachedFloatingLongValue airCompressor;
    private final ForgeConfigSpec configSpec;

    ChemistryUsageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Chemistry Energy Usage Config. This config is synced from server to client.").push("storage");

        airCompressor = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "airCompressor", FloatingLong.createConst(100));

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "chemistry-usage";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }

    @Override
    public boolean addToContainer() {
        return false;
    }
}
