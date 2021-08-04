package mekanism.common.config.value;

import java.util.HashSet;
import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedPrimitiveValue<T> {

    protected final ConfigValue<T> internal;
    private Set<Runnable> invalidationListeners;
    protected boolean resolved;

    protected CachedPrimitiveValue(IMekanismConfig config, ConfigValue<T> internal) {
        this.internal = internal;
        config.addCachedValue(this);
    }

    public void addInvalidationListener(Runnable listener) {
        if (invalidationListeners == null) {
            invalidationListeners = new HashSet<>();
        }
        if (!invalidationListeners.add(listener)) {
            Mekanism.logger.warn("Duplicate invalidation listener added");
        }
    }

    public void removeInvalidationListener(Runnable listener) {
        if (invalidationListeners == null || !invalidationListeners.remove(listener)) {
            Mekanism.logger.warn("Unable to remove specified invalidation listener.");
        }
    }

    public void clearCache() {
        resolved = false;
        if (invalidationListeners != null) {
            invalidationListeners.forEach(Runnable::run);
        }
    }
}