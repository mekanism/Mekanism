package mekanism.chemistry.common.config;

import mekanism.common.config.BaseMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ChemistryStorageConfig extends BaseMekanismConfig {

    private final ForgeConfigSpec configSpec;

    ChemistryStorageConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Chemistry Energy Storage Config. This config is synced from server to client.").push("storage");

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "chemistry-storage";
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
