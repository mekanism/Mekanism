package mekanism.tools.common.config;

import mekanism.common.config.MekanismConfig;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismToolsConfig {

    public static final ToolsConfig tools = new ToolsConfig();

    public static void loadFromFiles() {
        MekanismConfig.load(tools);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfig.registerConfig(modLoadingContext, tools);
    }
}