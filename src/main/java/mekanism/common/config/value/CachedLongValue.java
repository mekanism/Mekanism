package mekanism.common.config.value;

import java.util.function.LongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedLongValue extends CachedPrimitiveValue<Long> implements LongSupplier {

    private long cachedValue;

    private CachedLongValue(IMekanismConfig config, ConfigValue<Long> internal) {
        super(config, internal);
    }

    public static CachedLongValue wrap(IMekanismConfig config, ConfigValue<Long> internal) {
        return new CachedLongValue(config, internal);
    }

    public long get() {
        if (!resolved) {
            //If we don't have a cached value or need to resolve it again, get it from the actual ConfigValue
            cachedValue = internal.get();
            resolved = true;
        }
        return cachedValue;
    }

    @Override
    public long getAsLong() {
        return get();
    }

    public void set(long value) {
        internal.set(value);
        cachedValue = value;
    }
}