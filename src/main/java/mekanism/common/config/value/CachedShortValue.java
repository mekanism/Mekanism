package mekanism.common.config.value;

import mekanism.api.functions.ShortSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedShortValue extends CachedPrimitiveValue<Short> implements ShortSupplier {

    private short cachedValue;

    private CachedShortValue(IMekanismConfig config, ConfigValue<Short> internal) {
        super(config, internal);
    }

    public static CachedShortValue wrap(IMekanismConfig config, ConfigValue<Short> internal) {
        return new CachedShortValue(config, internal);
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
}