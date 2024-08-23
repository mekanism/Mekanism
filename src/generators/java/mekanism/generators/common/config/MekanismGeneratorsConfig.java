package mekanism.generators.common.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.generators.common.MekanismGenerators;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class MekanismGeneratorsConfig {

    private MekanismGeneratorsConfig() {
    }

    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();
    public static final GeneratorsConfig generators = new GeneratorsConfig();
    public static final GeneratorsGearConfig gear = new GeneratorsGearConfig();
    public static final GeneratorsStorageConfig storageConfig = new GeneratorsStorageConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, generators);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, gear);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, storageConfig);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        MekanismConfigHelper.onConfigLoad(configEvent, MekanismGenerators.MODID, KNOWN_CONFIGS);
    }

    public static Collection<IMekanismConfig> getConfigs() {
        return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
    }
}