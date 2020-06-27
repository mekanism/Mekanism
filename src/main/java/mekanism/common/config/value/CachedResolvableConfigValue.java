package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * @param <TYPE> The type this {@link CachedResolvableConfigValue} resolves to
 * @param <REAL> The real type that the {@link ConfigValue} holds
 */
public abstract class CachedResolvableConfigValue<TYPE, REAL> {

    private final ConfigValue<REAL> internal;
    private List<Runnable> invalidationListeners;
    @Nullable
    private TYPE cachedValue;

    protected CachedResolvableConfigValue(IMekanismConfig config, ConfigValue<REAL> internal) {
        this.internal = internal;
        config.addCachedValue(this);
    }

    public void addInvalidationListener(Runnable listener) {
        if (invalidationListeners == null) {
            invalidationListeners = new ArrayList<>();
        }
        invalidationListeners.add(listener);
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
            invalidationListeners.forEach(Runnable::run);
        }
    }
}