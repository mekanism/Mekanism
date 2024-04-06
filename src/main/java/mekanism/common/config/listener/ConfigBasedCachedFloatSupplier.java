package mekanism.common.config.listener;

import mekanism.api.functions.FloatSupplier;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedValue;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;

public class ConfigBasedCachedFloatSupplier implements FloatSupplier {

    private final FloatSupplier resolver;
    private boolean resolved;
    private float cachedValue;

    public ConfigBasedCachedFloatSupplier(FloatSupplier resolver, CachedFloatValue... dependantConfigValues) {
        this.resolver = resolver;
        IConfigValueInvalidationListener refreshListener = this::refresh;
        for (CachedValue<?> configValue : dependantConfigValues) {
            configValue.addInvalidationListener(refreshListener);
        }
    }

    protected final void refresh() {
        this.cachedValue = resolver.getAsFloat();
        resolved = true;
    }

    @Override
    public float getAsFloat() {
        if (!resolved) {
            //Lazily initialize the cached value so that we don't accidentally query values before they are initially set
            refresh();
        }
        return cachedValue;
    }
}