package mekanism.common.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class MekanismConfig {

    private MekanismConfig() {
    }

    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();
    public static final ClientConfig client = new ClientConfig();
    public static final CommonConfig common = new CommonConfig();
    public static final GeneralConfig general = new GeneralConfig();
    public static final GearConfig gear = new GearConfig();
    public static final MekanismStartupConfig startup = new MekanismStartupConfig();
    public static final StorageConfig storage = new StorageConfig();
    public static final TierConfig tiers = new TierConfig();
    public static final UsageConfig usage = new UsageConfig();
    public static final WorldConfig world = new WorldConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, client);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, common);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, general);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, gear);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, startup);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, storage);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, tiers);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, usage);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, world);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        MekanismConfigHelper.onConfigLoad(configEvent, Mekanism.MODID, KNOWN_CONFIGS);
    }

    public static Collection<IMekanismConfig> getConfigs() {
        return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
    }
}