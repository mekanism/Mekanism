package mekanism.common.config;

import mekanism.common.Mekanism;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Custom {@link ModConfig} implementation that makes invalidating custom caches easier.
 */
public class MekanismModConfig extends ModConfig {

    private final IMekanismConfig mekanismConfig;

    public MekanismModConfig(ModContainer container, IMekanismConfig config) {
        super(config.getConfigType(), config.getConfigSpec(), container, Mekanism.MOD_NAME + "/" + config.getFileName() + ".toml");
        this.mekanismConfig = config;
    }

    public void clearCache(ModConfigEvent event) {
        mekanismConfig.clearCache(event instanceof ModConfigEvent.Unloading);
    }
}