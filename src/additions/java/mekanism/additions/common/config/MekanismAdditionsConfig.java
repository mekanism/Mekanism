package mekanism.additions.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismAdditionsConfig {

    private MekanismAdditionsConfig() {
    }

    public static final AdditionsConfig additions = new AdditionsConfig();
    public static final AdditionsCommonConfig common = new AdditionsCommonConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, additions);
        MekanismConfigHelper.registerConfig(modContainer, common);
    }
}