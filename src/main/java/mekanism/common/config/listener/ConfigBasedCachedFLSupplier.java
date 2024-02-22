package mekanism.common.config.listener;

import java.util.function.Supplier;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.value.CachedValue;

public class ConfigBasedCachedFLSupplier extends ConfigBasedCachedSupplier<FloatingLong> implements FloatingLongSupplier {

    public ConfigBasedCachedFLSupplier(Supplier<FloatingLong> resolver, CachedValue<?>... dependantConfigValues) {
        super(resolver, dependantConfigValues);
    }
}