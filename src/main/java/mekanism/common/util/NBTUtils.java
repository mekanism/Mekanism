package mekanism.common.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.resource.IResourceContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class NBTUtils {//TODO - 26.1: Should we rename this class?

    private NBTUtils() {
    }

    //TODO - 26.1: Re-evaluate all these enum related methods and what cases should be replaced to use names instead of ordinals
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

    public static <RESOURCE extends Resource> void storeNonEmpty(ValueOutput output, String key, IResourceContainer<RESOURCE> container) {
        if (!container.isEmpty()) {
            container.stackHelper().storeNonEmpty(output, key, container.asStack());
        }
    }

    public static <RESOURCE extends Resource> void readOrEmpty(ValueInput input, String key, IResourceContainer<RESOURCE> container) {
        container.setContents(container.stackHelper().readOrEmpty(input, key), null);
    }
}