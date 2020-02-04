package mekanism.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.value.CachedConfigValue;
import mekanism.common.config.value.CachedPrimitiveValue;

public abstract class BaseMekanismConfig implements IMekanismConfig {

    private final List<CachedConfigValue<?>> cachedConfigValues = new ArrayList<>();
    private final List<CachedPrimitiveValue<?>> cachedPrimitiveValues = new ArrayList<>();

    @Override
    public void clearCache() {
        cachedConfigValues.forEach(CachedConfigValue::clearCache);
        cachedPrimitiveValues.forEach(CachedPrimitiveValue::clearCache);
    }

    @Override
    public <T> void addCachedValue(CachedConfigValue<T> configValue) {
        cachedConfigValues.add(configValue);
    }

    @Override
    public <T> void addCachedValue(CachedPrimitiveValue<T> configValue) {
        cachedPrimitiveValues.add(configValue);
    }
}