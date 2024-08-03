package mekanism.additions.common.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class MekanismAdditionsConfig {

    private MekanismAdditionsConfig() {
    }

    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();
    public static final AdditionsConfig additions = new AdditionsConfig();
    public static final AdditionsClientConfig client = new AdditionsClientConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, client);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, additions);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        MekanismConfigHelper.onConfigLoad(configEvent, MekanismAdditions.MODID, KNOWN_CONFIGS);
    }

    public static Collection<IMekanismConfig> getConfigs() {
        return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
    }
}