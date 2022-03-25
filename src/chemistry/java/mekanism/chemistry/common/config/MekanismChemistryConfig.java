package mekanism.chemistry.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismChemistryConfig {

    private MekanismChemistryConfig() {
    }

    public static final ChemistryConfig chemistry = new ChemistryConfig();
    public static final ChemistryStorageConfig storageConfig = new ChemistryStorageConfig();
    public static final ChemistryUsageConfig usageConfig = new ChemistryUsageConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, chemistry);
        MekanismConfigHelper.registerConfig(modContainer, storageConfig);
        MekanismConfigHelper.registerConfig(modContainer, usageConfig);
    }
}
