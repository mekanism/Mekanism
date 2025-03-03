package mekanism.common.config.value;

import mekanism.api.functions.ShortSupplier;
import mekanism.common.config.IMekanismConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedShortValue extends CachedValue<Short> implements ShortSupplier {

    private boolean resolved;
    private short cachedValue;

    private CachedShortValue(IMekanismConfig config, ConfigValue<Short> internal) {
        super(config, internal);
    }

    public static CachedShortValue wrap(IMekanismConfig config, ConfigValue<Short> internal) {
        return new CachedShortValue(config, internal);
    }

    public short getOrDefault() {
        if (resolved || isLoaded()) {
            return get();
        }
        return internal.getDefault();
    }

    public short get() {
        if (!resolved) {
            //If we don't have a cached value or need to resolve it again, get it from the actual ConfigValue
            cachedValue = internal.get();
            resolved = true;
        }
        return cachedValue;
    }

    @Override
    public short getAsShort() {
        return get();
    }

    public void set(short value) {
        internal.set(value);
        cachedValue = value;
    }

    @Override
    protected boolean clearCachedValue(boolean checkChanged) {
        if (!resolved) {
            //Isn't cached don't need to clear it or run any invalidation listeners
            return false;
        }
        short oldCachedValue = cachedValue;
        resolved = false;
        //Return if we are meant to check the changed ones, and it is different then it used to be
        return checkChanged && oldCachedValue != get();
    }
}