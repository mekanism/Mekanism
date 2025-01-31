package mekanism.common.config;

import mekanism.common.config.value.CachedValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IMekanismConfig {

    String getFileName();

    String getTranslation();

    ModConfigSpec getConfigSpec();

    default boolean isLoaded() {
        return getConfigSpec().isLoaded();
    }

    ModConfig.Type getConfigType();

    void save();

    void clearCache(boolean unloading);

    void addCachedValue(CachedValue<?> configValue);
}