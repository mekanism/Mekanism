package mekanism.common.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class NBTUtils {

    private NBTUtils() {
    }

    public static void copyViaSerialization(ProblemReporter.PathElement problemPath, HolderLookup.Provider lookup, ValueIOSerializable copyFrom, ValueIOSerializable copyTo) {
        //TODO - 1.21.11: Evaluate all uses and whether we want to use the scoped collector that doesn't provide a problem path?
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath, Mekanism.logger)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, lookup);
            copyFrom.serialize(output);
            ValueInput input = TagValueInput.create(reporter, lookup, output.buildResult());
            copyTo.deserialize(input);
        }
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