package mekanism.additions.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.neoforged.fml.ModContainer;

public class MekanismAdditionsConfig {

    private MekanismAdditionsConfig() {
    }

    public static final AdditionsConfig additions = new AdditionsConfig();
    public static final AdditionsClientConfig client = new AdditionsClientConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(modContainer, client);
        MekanismConfigHelper.registerConfig(modContainer, additions);
    }
}