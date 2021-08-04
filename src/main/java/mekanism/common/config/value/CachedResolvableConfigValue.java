package mekanism.common.config.value;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * @param <TYPE> The type this {@link CachedResolvableConfigValue} resolves to
 * @param <REAL> The real type that the {@link ConfigValue} holds
 */
public abstract class CachedResolvableConfigValue<TYPE, REAL> {

    private final ConfigValue<REAL> internal;
    private Set<Runnable> invalidationListeners;
    @Nullable
    private TYPE cachedValue;

    protected CachedResolvableConfigValue(IMekanismConfig config, ConfigValue<REAL> internal) {
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

    protected abstract TYPE resolve(REAL encoded);

    protected abstract REAL encode(TYPE value);

    @Nonnull
    public TYPE get() {
        if (cachedValue == null) {
            //If we don't have a cached value, resolve it from the actual ConfigValue
            cachedValue = resolve(internal.get());
        }
        return cachedValue;
    }

    public void set(TYPE value) {
        internal.set(encode(value));
        cachedValue = value;
    }

    public void clearCache() {
        cachedValue = null;
        if (invalidationListeners != null) {
            //TODO - 10.1: Should invalidation listeners (both here and primitive), immediately uncache
            // the value and only if it has changed rerun the invalidation listeners
            // given the majority of invalidation listeners likely would just be getting the value again
            // at which point if it stays the same and we can skip running it then it may be preferable
            // though this would make it so that invalidation listeners can't really be lazy unless we have
            // both types.
            // Maybe extend runnable have a defaulted boolean for if it grabs it instantly and then keep track
            // when adding/removing of count of listeners that are non lazy
            invalidationListeners.forEach(Runnable::run);
        }
    }
}