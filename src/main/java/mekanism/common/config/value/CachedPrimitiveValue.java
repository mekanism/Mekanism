package mekanism.common.config.value;

import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedPrimitiveValue<T> {

    protected final ConfigValue<T> internal;
    protected boolean resolved;

    protected CachedPrimitiveValue(IMekanismConfig config, ConfigValue<T> internal) {
        this.internal = internal;
        config.addCachedValue(this);
    }

    public void clearCache() {
        resolved = false;
    }
}