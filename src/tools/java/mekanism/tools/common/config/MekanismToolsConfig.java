package mekanism.tools.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismToolsConfig {

    private MekanismToolsConfig() {
    }

    public static final ToolsConfig tools = new ToolsConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfigHelper.registerConfig(modLoadingContext.getActiveContainer(), tools);
    }
}