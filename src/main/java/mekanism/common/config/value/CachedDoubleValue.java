package mekanism.common.config.value;

import java.util.function.DoubleSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CachedDoubleValue extends CachedPrimitiveValue<Double> implements DoubleSupplier {

    private double cachedValue;

    private CachedDoubleValue(IMekanismConfig config, ConfigValue<Double> internal) {
        super(config, internal);
    }

    public static CachedDoubleValue wrap(IMekanismConfig config, ConfigValue<Double> internal) {
        return new CachedDoubleValue(config, internal);
    }

    public double get() {
        if (!resolved) {
            //If we don't have a cached value or need to resolve it again, get it from the actual ConfigValue
            cachedValue = internal.get();
            resolved = true;
        }
        return cachedValue;
    }

    @Override
    public double getAsDouble() {
        return get();
    }

    public void set(double value) {
        internal.set(value);
        cachedValue = value;
    }
}