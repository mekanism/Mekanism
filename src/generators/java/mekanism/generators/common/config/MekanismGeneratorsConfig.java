package mekanism.generators.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismGeneratorsConfig {

    public static final GeneratorsConfig generators = new GeneratorsConfig();

    public static void loadFromFiles() {
        MekanismConfigHelper.load(generators);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfigHelper.registerConfig(modLoadingContext, generators);
    }
}