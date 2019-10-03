package mekanism.generators.common.config;

import mekanism.common.config.MekanismConfig;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismGeneratorsConfig {

    public static final GeneratorsConfig generators = new GeneratorsConfig();

    public static void loadFromFiles() {
        MekanismConfig.load(generators);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(generators.getConfigType(), generators.getConfigSpec());
    }
}