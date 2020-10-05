package mekanism.common.config;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismConfig {

    private MekanismConfig() {
    }

    public static final ClientConfig client = new ClientConfig();
    public static final GeneralConfig general = new GeneralConfig();
    public static final GearConfig gear = new GearConfig();
    public static final StorageConfig storage = new StorageConfig();
    public static final TierConfig tiers = new TierConfig();
    public static final UsageConfig usage = new UsageConfig();
    public static final WorldConfig world = new WorldConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, client);
        MekanismConfigHelper.registerConfig(modContainer, general);
        MekanismConfigHelper.registerConfig(modContainer, gear);
        MekanismConfigHelper.registerConfig(modContainer, storage);
        MekanismConfigHelper.registerConfig(modContainer, tiers);
        MekanismConfigHelper.registerConfig(modContainer, usage);
        MekanismConfigHelper.registerConfig(modContainer, world);
    }
}