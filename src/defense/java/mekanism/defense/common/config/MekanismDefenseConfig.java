package mekanism.defense.common.config;

import mekanism.common.config.MekanismConfigHelper;
import net.neoforged.fml.ModContainer;

public class MekanismDefenseConfig {

    private MekanismDefenseConfig() {
    }

    public static final DefenseConfig defense = new DefenseConfig();

    public static void registerConfigs(ModContainer modContainer) {
        MekanismConfigHelper.registerConfig(modContainer, defense);
    }
}