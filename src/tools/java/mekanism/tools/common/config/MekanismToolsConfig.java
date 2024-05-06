package mekanism.tools.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.neoforged.fml.ModContainer;

public class MekanismToolsConfig {

    private MekanismToolsConfig() {
    }

    public static final ToolsConfig tools = new ToolsConfig();
    public static final ToolsMaterialConfig materials = new ToolsMaterialConfig();
    public static final ToolsClientConfig toolsClient = new ToolsClientConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(modContainer, tools);
        MekanismConfigHelper.registerConfig(modContainer, materials);
        MekanismConfigHelper.registerConfig(modContainer, toolsClient);
    }
}