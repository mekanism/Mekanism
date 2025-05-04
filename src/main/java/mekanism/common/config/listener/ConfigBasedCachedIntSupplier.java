package mekanism.common.config.listener;

import java.util.function.IntSupplier;
import mekanism.common.config.value.CachedValue;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;

public class ConfigBasedCachedIntSupplier implements IntSupplier {

    private final IntSupplier resolver;
    private boolean resolved;
    private int cachedValue;

    public ConfigBasedCachedIntSupplier(IntSupplier resolver, CachedValue<?>... dependantConfigValues) {
        this.resolver = resolver;
        IConfigValueInvalidationListener refreshListener = this::refresh;
        for (CachedValue<?> configValue : dependantConfigValues) {
            configValue.addInvalidationListener(refreshListener);
        }
    }

    protected final void refresh() {
        this.cachedValue = resolver.getAsInt();
        resolved = true;
    }

    @Override
    public int getAsInt() {
        if (!resolved) {
            //Lazily initialize the cached value so that we don't accidentally query values before they are initially set
            refresh();
        }
        return cachedValue;
    }
}