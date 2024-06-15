package mekanism.common.config.value;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.Util;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.jetbrains.annotations.Nullable;

/**
 * A cached value implementation for representing Unsigned Longs as Strings. We use strings so that we can validate the data as well as allow us to represent
 * unsigned numbers properly.
 */
public class CachedUnsignedLongValue extends CachedResolvableConfigValue<Long, String> implements Supplier<Long> {

    //will reject negatives
    public static final Predicate<Object> VALIDATOR = object -> tryGetValue(object) != null;

    //public static final Predicate<Object> ENERGY_CONVERSION = Util.make(() -> {
    //    FloatingLong max = FloatingLong.ONE.divide(FloatingLong.createConst(0, (short) 1)).copyAsConst();//Inverse of min positive value
    //    return greaterZeroLessThan(max);
    //});
//
    //public static Predicate<Object> greaterZeroLessThan(FloatingLong max) {
    //    return object -> {
    //        FloatingLong value = tryGetValue(object);
    //        return value != null && value.greaterThan(FloatingLong.ZERO) && value.smallerOrEqual(max);
    //    };
    //}

    @Nullable
    private static Long tryGetValue(Object object) {
        if (object instanceof String string) {
            try {
                return Long.parseUnsignedLong(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private CachedUnsignedLongValue(IMekanismConfig config, ConfigValue<String> internal) {
        super(config, internal);
    }

    public static CachedUnsignedLongValue define(IMekanismConfig config, Builder builder, String comment, String path, long defaultValue) {
        return define(config, builder, comment, path, defaultValue, false);
    }

    public static CachedUnsignedLongValue define(IMekanismConfig config, Builder builder, String comment, String path, long defaultValue, boolean worldRestart) {
        return define(config, builder, comment, path, defaultValue, worldRestart, VALIDATOR);
    }

    public static CachedUnsignedLongValue define(IMekanismConfig config, Builder builder, String comment, String path, long defaultValue,
          Predicate<Object> validator) {
        return define(config, builder, comment, path, defaultValue, false, validator);
    }

    public static CachedUnsignedLongValue define(IMekanismConfig config, Builder builder, String comment, String path, long defaultValue, boolean worldRestart,
          Predicate<Object> validator) {
        if (worldRestart) {
            builder.worldRestart();
        }
        return new CachedUnsignedLongValue(config, builder.comment(comment).define(path, Long.toUnsignedString(defaultValue), validator));
    }

    @Override
    protected Long resolve(String encoded) {
        return Long.parseUnsignedLong(encoded);
    }

    @Override
    protected String encode(Long value) {
        return Long.toUnsignedString(value);
    }
}