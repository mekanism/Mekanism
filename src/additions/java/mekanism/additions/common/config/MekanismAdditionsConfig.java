package mekanism.additions.common.config;

import mekanism.common.config.MekanismConfig;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismAdditionsConfig {

    public static final AdditionsConfig additions = new AdditionsConfig();

    public static void loadFromFiles() {
        MekanismConfig.load(additions);
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(additions.getConfigType(), additions.getConfigSpec());
    }
}