package mekanism.common.config;

import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

//TODO: Hopefully remove the need for this, currently this acts as a wrapper around ConfigValue<Double>
// as reading values from the config sometimes causes class casting exceptions
public class FloatValue {

    private final ConfigValue<Double> value;

    private FloatValue(ConfigValue<Double> value) {
        this.value = value;
    }

    public static FloatValue of(ConfigValue<Double> value) {
        return new FloatValue(value);
    }

    public float get() {
        Double val = value.get();
        if (val == null) {
            //TODO: Should we throw an error here or what should we return
            return 0;
        }
        if (val > Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        } else if (val < Float.MIN_VALUE) {
            return Float.MIN_VALUE;
        }
        return val.floatValue();
    }
}