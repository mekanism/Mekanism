package mekanism.generators.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismGeneratorsConfig {

    private MekanismGeneratorsConfig() {
    }

    public static final GeneratorsConfig generators = new GeneratorsConfig();
    public static final GeneratorsGearConfig gear = new GeneratorsGearConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        ModContainer modContainer = modLoadingContext.getActiveContainer();
        MekanismConfigHelper.registerConfig(modContainer, generators);
        MekanismConfigHelper.registerConfig(modContainer, gear);
    }
}