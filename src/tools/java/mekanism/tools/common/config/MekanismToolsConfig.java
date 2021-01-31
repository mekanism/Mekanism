package mekanism.tools.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismToolsConfig {

    private MekanismToolsConfig() {
    }

    public static final ToolsConfig tools = new ToolsConfig();
    public static final ToolsClientConfig toolsClient = new ToolsClientConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, tools);
        MekanismConfigHelper.registerConfig(modContainer, toolsClient);
    }
}