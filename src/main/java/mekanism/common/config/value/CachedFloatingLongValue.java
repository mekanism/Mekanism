package mekanism.common.config.value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

//TODO: Decide how we want to declare this, do we want to make it be wrapping a ConfigValue<String> or do we want it wrapping a two element list
// or do we want it wrapping two separate ConfigValue objects, one for long one for short
public class CachedFloatingLongValue extends CachedResolvableConfigValue<FloatingLong, List<? extends Number>> implements FloatingLongSupplier {

    //TODO: FIXME and the default validator... as we don't have a FloatingLong as our object, we have an encoded list
    //TODO: Also make our define methods define actual lists, as it is doing it wrong currently
    public static final Predicate<Object> POSITIVE = object -> {
        if (object instanceof Number) {
            //TODO: Technically we don't want the specific elements to be zero here. But one can be zero if the other is not
            return ((Number) object).doubleValue() >= 0;
        }
        return false;
    };
    private static final Predicate<Object> VALIDATOR = object -> {
        if (object instanceof Number) {
            return ((Number) object).doubleValue() >= 0;
        }
        return false;
    };

    public static List<? extends Number> encodeValue(FloatingLong value) {
        List<Number> actual = new ArrayList<>();
        actual.add(value.getValue());
        actual.add(value.getDecimal());
        return actual;
    }

    private CachedFloatingLongValue(IMekanismConfig config, ConfigValue<List<? extends Number>> internal) {
        super(config, internal);
    }

    public static CachedFloatingLongValue define(IMekanismConfig config, ForgeConfigSpec.Builder builder, String comment, String path, FloatingLong defaultValue) {
        return define(config, builder, comment, path, defaultValue, false);
    }

    public static CachedFloatingLongValue define(IMekanismConfig config, ForgeConfigSpec.Builder builder, String comment, String path, FloatingLong defaultValue, boolean worldRestart) {
        return define(config, builder, comment, path, defaultValue, worldRestart, VALIDATOR);
    }

    public static CachedFloatingLongValue define(IMekanismConfig config, ForgeConfigSpec.Builder builder, String comment, String path, FloatingLong defaultValue,
          Predicate<Object> validator) {
        return define(config, builder, comment, path, defaultValue, false, validator);
    }

    public static CachedFloatingLongValue define(IMekanismConfig config, Builder builder, String comment, String path, FloatingLong defaultValue, boolean worldRestart,
          Predicate<Object> validator) {
        if (worldRestart) {
            builder.worldRestart();
        }
        return new CachedFloatingLongValue(config, builder.comment(comment).defineList(path, encodeValue(defaultValue), validator));
    }

    @Override
    protected FloatingLong resolve(List<? extends Number> encoded) {
        int size = encoded.size();
        long value = 0;
        short decimal = 0;
        if (size > 0) {
            value = encoded.get(0).longValue();
            if (size > 1) {
                decimal = encoded.get(1).shortValue();
            }
        }
        return FloatingLong.create(value, decimal);
    }

    @Override
    protected List<? extends Number> encode(FloatingLong value) {
        return encodeValue(value);
    }
}