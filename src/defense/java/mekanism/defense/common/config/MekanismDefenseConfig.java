package mekanism.defense.common.config;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.defense.common.MekanismDefense;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class MekanismDefenseConfig {

    private MekanismDefenseConfig() {
    }

    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();
    public static final DefenseConfig defense = new DefenseConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, defense);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        MekanismConfigHelper.onConfigLoad(configEvent, MekanismDefense.MODID, KNOWN_CONFIGS);
    }
}