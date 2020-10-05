package mekanism.defense.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.minecraftforge.fml.ModLoadingContext;

public class MekanismDefenseConfig {

    private MekanismDefenseConfig() {
    }

    public static final DefenseConfig defense = new DefenseConfig();

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        MekanismConfigHelper.registerConfig(modLoadingContext.getActiveContainer(), defense);
    }
}