package mekanism.common.config.value;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.IMekanismConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * A cached value implementation for representing {@link FloatingLong}s as Strings. We use strings so that we can validate the data as well as allow us to represent
 * unsigned numbers properly.
 */
public class CachedFloatingLongValue extends CachedResolvableConfigValue<FloatingLong, String> implements FloatingLongSupplier {

    private static final Predicate<Object> VALIDATOR = object -> tryGetValue(object) != null;
    public static final Predicate<Object> POSITIVE = object -> {
        FloatingLong value = tryGetValue(object);
        return value != null && value.greaterThan(FloatingLong.ZERO);
    };

    @Nullable
    private static FloatingLong tryGetValue(Object object) {
        if (object instanceof String) {
            try {
                return FloatingLong.parseFloatingLong((String) object);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private CachedFloatingLongValue(IMekanismConfig config, ConfigValue<String> internal) {
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
        return new CachedFloatingLongValue(config, builder.comment(comment).define(path, defaultValue.toString(), validator));
    }

    @Override
    protected FloatingLong resolve(String encoded) {
        return FloatingLong.parseFloatingLong(encoded, true);
    }

    @Override
    protected String encode(FloatingLong value) {
        return value.toString();
    }
}