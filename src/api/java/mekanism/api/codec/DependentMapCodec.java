package mekanism.api.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MapCodec where the element type is dependent on another field
 *
 * @param <ELEMENT>    the end result of this MapCodec
 * @param <DEPENDENCY> the extra data needed to determine the type of ELEMENT
 */
public class DependentMapCodec<ELEMENT, DEPENDENCY> extends MapCodec<ELEMENT> {

    private final String fieldName;
    private final Function<DEPENDENCY, Codec<ELEMENT>> elementCodecGetter;
    private final MapCodec<DEPENDENCY> dependencyMapCodec;
    private final Function<ELEMENT, DEPENDENCY> dependencyGetter;

    /**
     * @param fieldName          the name of the field for ELEMENT
     * @param elementCodecGetter Supplies the correct Codec for the value of ELEMENT
     * @param dependencyMapCodec MapCodec for the extra data (determines its field name and element codec)
     * @param dependencyGetter   Supplies the extra data for the supplied input
     */
    public DependentMapCodec(final String fieldName, final Function<DEPENDENCY, Codec<ELEMENT>> elementCodecGetter, MapCodec<DEPENDENCY> dependencyMapCodec, Function<ELEMENT, DEPENDENCY> dependencyGetter) {
        this.fieldName = fieldName;
        this.elementCodecGetter = elementCodecGetter;
        this.dependencyMapCodec = dependencyMapCodec;
        this.dependencyGetter = dependencyGetter;
    }

    @Override
    public <T> RecordBuilder<T> encode(final ELEMENT input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        //find the dependency
        DEPENDENCY dependency = dependencyGetter.apply(input);
        //encode ELEMENT after asking the getter what codec to use
        RecordBuilder<T> newPrefix = prefix.add(fieldName, elementCodecGetter.apply(dependency).encodeStart(ops, input));
        //encode the extra data
        return dependencyMapCodec.encode(dependency, ops, newPrefix);
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.concat(Stream.of(ops.createString(fieldName)), dependencyMapCodec.keys(ops));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DependentMapCodec<?, ?> that = (DependentMapCodec<?, ?>) o;
        return Objects.equals(fieldName, that.fieldName) && Objects.equals(elementCodecGetter, that.elementCodecGetter) && Objects.equals(dependencyMapCodec, that.dependencyMapCodec) && Objects.equals(dependencyGetter, that.dependencyGetter);
    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + elementCodecGetter.hashCode();
        result = 31 * result + dependencyMapCodec.hashCode();
        result = 31 * result + dependencyGetter.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "mekanism:DependentMapCodec[" + fieldName + ": " + dependencyMapCodec.keys(JsonOps.INSTANCE).map(JsonElement::getAsString).distinct().collect(Collectors.joining(",")) + ']';
    }

    @Override
    public <T> DataResult<ELEMENT> decode(DynamicOps<T> ops, MapLike<T> input) {
        final T value = input.get(fieldName);
        //bail early if there was nothing present for our field
        if (value == null) {
            return DataResult.error(() -> "No key " + fieldName + " in " + input);
        }
        return dependencyMapCodec.decode(ops, input)//decode the extra data
              .map(elementCodecGetter)//query the element codec to use
              .flatMap(elementEncoder -> elementEncoder.parse(ops, value));//decode it, error condition is extra, then ours if no extra error
    }
}
