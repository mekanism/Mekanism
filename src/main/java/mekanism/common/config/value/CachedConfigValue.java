package mekanism.common.config.value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedConfigValue<T> {

    private final ConfigValue<T> internal;
    @Nullable
    private T cachedValue;

    protected CachedConfigValue(IMekanismConfig config, ConfigValue<T> internal) {
        this.internal = internal;
        config.addCachedValue(this);
    }

    public static <T> CachedConfigValue<T> wrap(IMekanismConfig config, ConfigValue<T> internal) {
        return new CachedConfigValue<>(config, internal);
    }

    @Nonnull
    public T get() {
        if (cachedValue == null) {
            //If we don't have a cached value, get it from the actual ConfigValue
            cachedValue = internal.get();
        }
        return cachedValue;
    }

    public void set(T value) {
        internal.set(value);
        cachedValue = value;
    }

    public void clearCache() {
        cachedValue = null;
    }
}