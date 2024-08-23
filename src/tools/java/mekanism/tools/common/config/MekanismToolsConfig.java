package mekanism.tools.common.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.MekanismConfigHelper;
import mekanism.tools.common.MekanismTools;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

public class MekanismToolsConfig {

    private MekanismToolsConfig() {
    }

    private static final Map<IConfigSpec, IMekanismConfig> KNOWN_CONFIGS = new HashMap<>();
    //Note: Materials has to be registered above tools, so that we can reference the materials in the other config
    public static final ToolsMaterialConfig materials = new ToolsMaterialConfig();
    public static final ToolsConfig tools = new ToolsConfig();
    public static final ToolsClientConfig toolsClient = new ToolsClientConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, materials);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, tools);
        MekanismConfigHelper.registerConfig(KNOWN_CONFIGS, modContainer, toolsClient);
    }

    public static void onConfigLoad(ModConfigEvent configEvent) {
        MekanismConfigHelper.onConfigLoad(configEvent, MekanismTools.MODID, KNOWN_CONFIGS);
    }

    public static Collection<IMekanismConfig> getConfigs() {
        return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
    }
}