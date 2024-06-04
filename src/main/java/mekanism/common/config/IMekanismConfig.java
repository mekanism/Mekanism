package mekanism.common.config;

import mekanism.common.config.value.CachedValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IMekanismConfig {

    String getFileName();

    ModConfigSpec getConfigSpec();

    default boolean isLoaded() {
        return getConfigSpec().isLoaded();
    }

    ModConfig.Type getConfigType();

    default void save() {
        getConfigSpec().save();
    }

    void clearCache(boolean unloading);

    void addCachedValue(CachedValue<?> configValue);

    /**
     * Should this config be added to the mods "config" files. Make this return false to only create the config. This will allow it to be tracked, but not override the
     * value that has already been added to this mod's container. As the list is from config type to mod config.
     */
    default boolean addToContainer() {
        return true;
    }
}