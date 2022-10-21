package mekanism.common.config.listener;

import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.value.CachedValue;
import net.minecraftforge.common.util.NonNullSupplier;

public class ConfigBasedCachedFLSupplier extends ConfigBasedCachedSupplier<FloatingLong> implements FloatingLongSupplier {

    public ConfigBasedCachedFLSupplier(NonNullSupplier<FloatingLong> resolver, CachedValue<?>... dependantConfigValues) {
        super(resolver, dependantConfigValues);
    }
}