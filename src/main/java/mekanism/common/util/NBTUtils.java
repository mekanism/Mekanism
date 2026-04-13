package mekanism.common.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class NBTUtils {

    private NBTUtils() {
    }

    //TODO - 1.21.11: Re-evaluate all these enum related methods and what cases should be replaced to use names instead of ordinals
    @Nullable
    public static <ENUM extends Enum<ENUM>> ENUM getEnum(ValueInput input, String key, IntFunction<ENUM> indexLookup) {
        Optional<Integer> value = input.getInt(key);
        //noinspection OptionalIsPresent - Capturing lambda
        if (value.isPresent()) {
            return indexLookup.apply(value.get());
        }
        return null;
    }

    public static <ENUM extends Enum<ENUM>> void setEnumIfPresent(ValueInput input, String key, IntFunction<ENUM> indexLookup, Consumer<ENUM> setter) {
        ENUM value = getEnum(input, key, indexLookup);
        if (value != null) {
            setter.accept(value);
        }
    }

    public static void writeEnum(ValueOutput output, String key, Enum<?> e) {
        output.putInt(key, e.ordinal());
    }
}