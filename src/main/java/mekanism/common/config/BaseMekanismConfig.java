package mekanism.common.config;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.config.value.CachedValue;

public abstract class BaseMekanismConfig implements IMekanismConfig {

    private final List<CachedValue<?>> cachedConfigValues = new ArrayList<>();

    @Override
    public void clearCache() {
        cachedConfigValues.forEach(CachedValue::clearCache);
    }

    @Override
    public void addCachedValue(CachedValue<?> configValue) {
        cachedConfigValues.add(configValue);
    }
}