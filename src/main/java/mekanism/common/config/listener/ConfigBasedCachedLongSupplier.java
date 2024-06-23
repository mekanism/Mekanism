package mekanism.common.config.listener;

import java.util.function.LongSupplier;
import mekanism.common.config.value.CachedValue;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;

public class ConfigBasedCachedLongSupplier implements LongSupplier {

    private final LongSupplier resolver;
    private boolean resolved;
    private long cachedValue;

    public ConfigBasedCachedLongSupplier(LongSupplier resolver, CachedValue<?>... dependantConfigValues) {
        this.resolver = resolver;
        IConfigValueInvalidationListener refreshListener = this::refresh;
        for (CachedValue<?> configValue : dependantConfigValues) {
            configValue.addInvalidationListener(refreshListener);
        }
    }

    protected final void refresh() {
        this.cachedValue = resolver.getAsLong();
        resolved = true;
    }

    @Override
    public long getAsLong() {
        if (!resolved) {
            //Lazily initialize the cached value so that we don't accidentally query values before they are initially set
            refresh();
        }
        return cachedValue;
    }
}