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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.container.LargeResourceStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
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

    private static final Consumer<String> ON_STACK_LOAD_ERROR = error -> MekanismAPI.logger.error("Tried to load invalid item: '{}'", error);
    /**
     * Helper codec to deserialize an optional item stack and fall back to the empty stack if an error is encountered in deserialization.
     *
     * @since 10.7.9
     */
    public static final Codec<ItemStack> LENIENT_OPTIONAL_STACK_CODEC = ItemStack.OPTIONAL_CODEC
          .promotePartial(ON_STACK_LOAD_ERROR)
          .orElse(ItemStack.EMPTY);
    /**
     * Helper codec to deserialize an optional item stack and fall back to the empty stack if an error is encountered in deserialization.
     *
     * @since 10.7.9
     * @deprecated Use an ItemStackTemplate instead
     */
    @Deprecated(forRemoval = true, since = "10.8.0")
    public static final Codec<ItemStack> OPTIONAL_SINGLE_ITEM_CODEC = ExtraCodecs.optionalEmptyMap(ItemStack.CODEC)
          .xmap(stack -> stack.orElse(ItemStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack.copyWithCount(1)));
    /**
     * Helper codec to deserialize an optional item stack with a constant count of one and fall back to the empty stack if an error is encountered in deserialization.
     *
     * @since 10.7.9
     * @deprecated Use an ItemStackTemplate instead
     */
    @Deprecated(forRemoval = true, since = "10.8.0")
    public static final Codec<ItemStack> LENIENT_OPTIONAL_SINGLE_ITEM_CODEC = OPTIONAL_SINGLE_ITEM_CODEC
          .promotePartial(ON_STACK_LOAD_ERROR)
          .orElse(ItemStack.EMPTY);
    /**
     * Helper codec to deserialize an optional fluid stack and fall back to the empty stack if an error is encountered in deserialization.
     *
     * @since 10.7.9
     */
    public static final Codec<FluidStack> LENIENT_OPTIONAL_FLUID_CODEC = FluidStack.OPTIONAL_CODEC
          .promotePartial(error -> MekanismAPI.logger.error("Tried to load invalid fluid: '{}'", error))
          .orElse(FluidStack.EMPTY);

    /**
     * Custom codec to allow serializing an item stack without the upper bounds.
     *
     * @since 10.6.1
     */
    public static final Codec<ItemStack> OVERSIZED_ITEM_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
          Item.CODEC.fieldOf(ItemInstance.FIELD_ID).forGetter(ItemStack::typeHolder),
          ExtraCodecs.POSITIVE_INT.fieldOf(ItemInstance.FIELD_COUNT).orElse(1).forGetter(ItemInstance::count),
          DataComponentPatch.CODEC.optionalFieldOf(ItemInstance.FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(instance, ItemStack::new)));


    //TODO - 26.1: Docs and decide where we want to store these
    // Modify tests that test the attached items to double check it handles empty stacks in general fine
    public static final Codec<LargeResourceStack<ItemResource>> ITEM_RESOURCE_STACK_CODEC = LargeResourceStack.codec(ItemResource.CODEC);
    public static final Codec<LargeResourceStack<ItemResource>> OPTIONAL_ITEM_RESOURCE_STACK_CODEC = makeOptionalCodec(ITEM_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_ITEM_STACK);
    public static final Codec<LargeResourceStack<ItemResource>> LENIENT_OPTIONAL_ITEM_RESOURCE_STACK_CODEC = makeLenientOptionalCodec(OPTIONAL_ITEM_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_ITEM_STACK);
    public static final StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<ItemResource>> ITEM_RESOURCE_STACK_STREAM_CODEC = LargeResourceStack.streamCodec(ItemResource.STREAM_CODEC, LargeResourceStack.EMPTY_ITEM_STACK);

    public static final Codec<LargeResourceStack<FluidResource>> FLUID_RESOURCE_STACK_CODEC = LargeResourceStack.codec(FluidResource.CODEC);
    public static final Codec<LargeResourceStack<FluidResource>> OPTIONAL_FLUID_RESOURCE_STACK_CODEC = makeOptionalCodec(FLUID_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_FLUID_STACK);
    public static final Codec<LargeResourceStack<FluidResource>> LENIENT_OPTIONAL_FLUID_RESOURCE_STACK_CODEC = makeLenientOptionalCodec(OPTIONAL_FLUID_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_FLUID_STACK);
    public static final StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<FluidResource>> FLUID_RESOURCE_STACK_STREAM_CODEC = LargeResourceStack.streamCodec(FluidResource.STREAM_CODEC, LargeResourceStack.EMPTY_FLUID_STACK);

    public static final Codec<LargeResourceStack<ChemicalResource>> CHEMICAL_RESOURCE_STACK_CODEC = LargeResourceStack.codec(ChemicalResource.CODEC);
    public static final Codec<LargeResourceStack<ChemicalResource>> OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC = makeOptionalCodec(CHEMICAL_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_CHEMICAL_STACK);
    public static final Codec<LargeResourceStack<ChemicalResource>> LENIENT_OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC = makeLenientOptionalCodec(OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC, LargeResourceStack.EMPTY_CHEMICAL_STACK);
    public static final StreamCodec<RegistryFriendlyByteBuf, LargeResourceStack<ChemicalResource>> CHEMICAL_RESOURCE_STACK_STREAM_CODEC = LargeResourceStack.streamCodec(ChemicalResource.STREAM_CODEC, LargeResourceStack.EMPTY_CHEMICAL_STACK);

    private static <RESOURCE extends Resource> Codec<LargeResourceStack<RESOURCE>> makeOptionalCodec(Codec<LargeResourceStack<RESOURCE>> stackCodec, LargeResourceStack<RESOURCE> emptyStack) {
        return ExtraCodecs.optionalEmptyMap(stackCodec)
              .xmap(optional -> optional.orElse(emptyStack), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    }

    private static <RESOURCE extends Resource> Codec<LargeResourceStack<RESOURCE>> makeLenientOptionalCodec(Codec<LargeResourceStack<RESOURCE>> stackCodec, LargeResourceStack<RESOURCE> emptyStack) {
        return stackCodec.promotePartial(error -> MekanismAPI.logger.error("Tried to load invalid resource: '{}'", error))
              .orElse(emptyStack);
    }

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

    public static <T> String stringify(Codec<T> codec, T value) {
        return codec.encodeStart(JsonOps.INSTANCE, value)
              .result()
              .map(JsonElement::toString)
              .orElse(value.toString());
    }
}