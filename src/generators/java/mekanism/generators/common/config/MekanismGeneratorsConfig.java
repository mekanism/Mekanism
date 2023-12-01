package mekanism.generators.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.neoforged.fml.ModContainer;

public class MekanismGeneratorsConfig {

    private MekanismGeneratorsConfig() {
    }

    public static final GeneratorsConfig generators = new GeneratorsConfig();
    public static final GeneratorsGearConfig gear = new GeneratorsGearConfig();
    public static final GeneratorsStorageConfig storageConfig = new GeneratorsStorageConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(modContainer, generators);
        MekanismConfigHelper.registerConfig(modContainer, gear);
        MekanismConfigHelper.registerConfig(modContainer, storageConfig);
    }
}