package mekanism.common.config.listener;

import java.util.function.Supplier;
import mekanism.common.config.value.CachedValue;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigBasedCachedSupplier<VALUE> implements NonNullSupplier<VALUE>, Supplier<VALUE> {

    private final NonNullSupplier<VALUE> resolver;
    @Nullable
    private VALUE cachedValue;

    public ConfigBasedCachedSupplier(NonNullSupplier<VALUE> resolver, CachedValue<?>... dependantConfigValues) {
        this.resolver = resolver;
        IConfigValueInvalidationListener refreshListener = this::refresh;
        for (CachedValue<?> configValue : dependantConfigValues) {
            configValue.addInvalidationListener(refreshListener);
        }
    }

    protected final void refresh() {
        this.cachedValue = resolver.get();
    }

    @NotNull
    @Override
    public VALUE get() {
        if (cachedValue == null) {
            //Lazily initialize the cached value so that we don't accidentally query values before they are initially set
            refresh();
        }
        return cachedValue;
    }
}