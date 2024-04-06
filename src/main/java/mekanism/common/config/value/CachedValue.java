package mekanism.common.config.value;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiPredicate;
import mekanism.common.Mekanism;
import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public abstract class CachedValue<T> {

    private final IMekanismConfig config;
    protected final ConfigValue<T> internal;
    private Set<IConfigValueInvalidationListener> invalidationListeners;

    protected CachedValue(IMekanismConfig config, ConfigValue<T> internal) {
        this.config = config;
        this.internal = internal;
        this.config.addCachedValue(this);
    }

    public boolean hasInvalidationListeners() {
        return invalidationListeners != null && !invalidationListeners.isEmpty();
    }

    public void addInvalidationListener(IConfigValueInvalidationListener listener) {
        if (invalidationListeners == null) {
            invalidationListeners = new HashSet<>();
        }
        if (!invalidationListeners.add(listener)) {
            Mekanism.logger.warn("Duplicate invalidation listener added");
        }
    }

    public void removeInvalidationListener(IConfigValueInvalidationListener listener) {
        if (invalidationListeners == null) {
            Mekanism.logger.warn("Unable to remove specified invalidation listener, no invalidation listeners have been added.");
        } else if (!invalidationListeners.remove(listener)) {
            Mekanism.logger.warn("Unable to remove specified invalidation listener.");
        }
    }

    public <DATA> void removeInvalidationListenersMatching(DATA data, BiPredicate<IConfigValueInvalidationListener, DATA> checker) {
        if (invalidationListeners != null && !invalidationListeners.isEmpty()) {
            //noinspection Java8CollectionRemoveIf - Capturing lambda
            for (Iterator<IConfigValueInvalidationListener> iter = invalidationListeners.iterator(); iter.hasNext(); ) {
                IConfigValueInvalidationListener listener = iter.next();
                if (checker.test(listener, data)) {
                    iter.remove();
                }
            }
        }
    }

    protected abstract boolean clearCachedValue(boolean checkChanged);

    public final void clearCache(boolean unloading) {
        if (hasInvalidationListeners()) {
            //Only clear cached values that have invalidation listeners if the config is loaded, as if it isn't loaded then
            // we will fail to clear the cache when we check for if the values have changed. Having a few config values using
            // slightly extra memory, and invalid values shouldn't matter as the config should only be used if it is loaded
            // so once the config is loaded there should be another clearCache call that then causes these values to get updated
            if (!unloading && isLoaded() && clearCachedValue(true)) {
                invalidationListeners.forEach(IConfigValueInvalidationListener::run);
            }
        } else {
            clearCachedValue(false);
        }
    }

    protected boolean isLoaded() {
        return config.isLoaded();
    }

    @FunctionalInterface
    public interface IConfigValueInvalidationListener extends Runnable {
        //Note: If we ever have any invalidation listeners that end up being lazy we can easily add a method to this
        // to specify it and then not bother regrabbing the value instantly
    }
}