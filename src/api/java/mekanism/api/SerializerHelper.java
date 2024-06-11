package mekanism.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder.Implementation;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

//TODO - 1.21: Update the wiki docs to fix the syntax
@NothingNullByDefault
public class SerializerHelper {

    private SerializerHelper() {
    }

    /**
     * Long Codec which accepts a number >= 0
     */
    public static final Codec<Long> POSITIVE_LONG_CODEC = Util.make(() -> {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(0L, Long.MAX_VALUE);
        return Codec.LONG.flatXmap(checker, checker);
    });

    /**
     * Long Codec which accepts a number > 0
     */
    public static final Codec<Long> POSITIVE_NONZERO_LONG_CODEC = Util.make(() -> {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(1L, Long.MAX_VALUE);
        return Codec.LONG.flatXmap(checker, checker);
    });

    /**
     * Generate a RecordCodecBuilder which is required only if the 'primary' is present. If this field is present, it will be returned regardless. Does not eat errors
     *
     * @param primaryField    the field which determines the required-ness. MUST be an Optional
     * @param dependentCodec  the codec for <strong>this</strong> field
     * @param dependentGetter the getter for this field (what you'd use on {@link MapCodec#forGetter(Function)})
     * @param <SOURCE>        the resulting type that both fields exist on
     * @param <THIS_TYPE>     the value type of this dependent field
     *
     * @return a RecordCodecBuilder which contains the resulting logic - use in side a `group()`
     */
    @NotNull
    public static <SOURCE, THIS_TYPE> RecordCodecBuilder<SOURCE, Optional<THIS_TYPE>> dependentOptionality(RecordCodecBuilder<SOURCE, ? extends Optional<?>> primaryField,
          MapCodec<Optional<THIS_TYPE>> dependentCodec, Function<SOURCE, Optional<THIS_TYPE>> dependentGetter) {
        Implementation<Optional<THIS_TYPE>> dependentRequired = new Implementation<>() {
            @Override
            public <T> DataResult<Optional<THIS_TYPE>> decode(DynamicOps<T> ops, MapLike<T> input) {
                DataResult<Optional<THIS_TYPE>> thisField = dependentCodec.decode(ops, input);

                //if the unboxed optional has a value, return this field's value.
                //if it had an error, return that
                if (thisField.error().isPresent() || thisField.result().orElse(Optional.empty()).isPresent()) {
                    return thisField;
                }

                //thisField must not be empty
                return DataResult.error(() -> "Missing value");
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return dependentCodec.keys(ops);
            }
        };
        return primaryField.dependent(dependentGetter, dependentCodec, primaryValue -> primaryValue.isEmpty() ? dependentCodec : dependentRequired);
    }

    /**
     * Generate a RecordCodecBuilder which is REQUIRED only if the 'other' is NOT present. When the other field is present, this one is OPTIONAL. Does not eat errors.
     *
     * @param otherField      the field which determines the required-ness. MUST be an Optional
     * @param dependentCodec  the codec for <strong>this</strong> field
     * @param dependentGetter the getter for this field (what you'd use on {@link MapCodec#forGetter(Function)})
     * @param <SOURCE>        the resulting type that both fields exist on
     * @param <THIS_TYPE>     the value type of this dependent field
     *
     * @return a RecordCodecBuilder which contains the resulting logic - use in side a `group()`
     */
    @NotNull
    public static <SOURCE, THIS_TYPE> RecordCodecBuilder<SOURCE, Optional<THIS_TYPE>> oneRequired(RecordCodecBuilder<SOURCE, ? extends Optional<?>> otherField,
          MapCodec<Optional<THIS_TYPE>> dependentCodec, Function<SOURCE, Optional<THIS_TYPE>> dependentGetter) {
        Implementation<Optional<THIS_TYPE>> dependentRequired = new Implementation<>() {
            @Override
            public <T> DataResult<Optional<THIS_TYPE>> decode(DynamicOps<T> ops, MapLike<T> input) {
                DataResult<Optional<THIS_TYPE>> thisField = dependentCodec.decode(ops, input);

                //if the unboxed optional has a value, return this field's value.
                //if it had an error, return that
                if (thisField.error().isPresent() || thisField.result().orElse(Optional.empty()).isPresent()) {
                    return thisField;
                }

                //the primary is empty, and this is also empty
                return DataResult.error(() -> getFieldNames(dependentCodec) + " is required");
            }

            private static <THIS_TYPE> String getFieldNames(MapCodec<Optional<THIS_TYPE>> codec) {
                return codec.keys(JsonOps.INSTANCE).map(JsonElement::getAsString).collect(Collectors.joining());
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return dependentCodec.keys(ops);
            }
        };
        return otherField.dependent(dependentGetter, dependentCodec, primaryValue -> primaryValue.isPresent() ? dependentCodec : dependentRequired);
    }
}