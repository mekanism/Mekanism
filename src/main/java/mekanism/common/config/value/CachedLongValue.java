package mekanism.common.config.value;

import java.util.function.LongSupplier;
import mekanism.common.config.IConfigTranslation;
import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedLongValue extends CachedValue<Long> implements LongSupplier {

    private boolean resolved;
    private long cachedValue;

    private CachedLongValue(IMekanismConfig config, ConfigValue<Long> internal) {
        super(config, internal);
    }

    public static CachedLongValue wrap(IMekanismConfig config, ConfigValue<Long> internal) {
        return new CachedLongValue(config, internal);
    }

    public static CachedLongValue definePositive(IMekanismConfig config, Builder builder, IConfigTranslation comment, String path, long defaultValue) {
        return define(config, builder, comment, path, defaultValue, 0, Long.MAX_VALUE);
    }

    public static CachedLongValue definedMin(IMekanismConfig config, Builder builder, IConfigTranslation comment, String path, long defaultValue, long min) {
        return define(config, builder, comment, path, defaultValue, min, Long.MAX_VALUE);
    }

    public static CachedLongValue define(IMekanismConfig config, Builder builder, IConfigTranslation comment, String path, long defaultValue, long min, long max) {
        return wrap(config, comment.applyToBuilder(builder).defineInRange(path, defaultValue, min, max));
    }

    public long getOrDefault() {
        if (resolved || isLoaded()) {
            return get();
        }
        return internal.getDefault();
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

    @Override
    protected boolean clearCachedValue(boolean checkChanged) {
        if (!resolved) {
            //Isn't cached don't need to clear it or run any invalidation listeners
            return false;
        }
        long oldCachedValue = cachedValue;
        resolved = false;
        //Return if we are meant to check the changed ones, and it is different then it used to be
        return checkChanged && oldCachedValue != get();
    }
}