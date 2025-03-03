package mekanism.common.config.value;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedIntValue extends CachedValue<Integer> implements IntSupplier, LongSupplier {

    private boolean resolved;
    private int cachedValue;

    private CachedIntValue(IMekanismConfig config, ConfigValue<Integer> internal) {
        super(config, internal);
    }

    public static CachedIntValue wrap(IMekanismConfig config, ConfigValue<Integer> internal) {
        return new CachedIntValue(config, internal);
    }

    public int getOrDefault() {
        if (resolved || isLoaded()) {
            return get();
        }
        return internal.getDefault();
    }

    public int get() {
        if (!resolved) {
            //If we don't have a cached value or need to resolve it again, get it from the actual ConfigValue
            cachedValue = internal.get();
            resolved = true;
        }
        return cachedValue;
    }

    @Override
    public int getAsInt() {
        return get();
    }

    @Override
    public long getAsLong() {
        return get();
    }

    public void set(int value) {
        internal.set(value);
        cachedValue = value;
    }

    @Override
    protected boolean clearCachedValue(boolean checkChanged) {
        if (!resolved) {
            //Isn't cached don't need to clear it or run any invalidation listeners
            return false;
        }
        int oldCachedValue = cachedValue;
        resolved = false;
        //Return if we are meant to check the changed ones, and it is different then it used to be
        return checkChanged && oldCachedValue != get();
    }
}