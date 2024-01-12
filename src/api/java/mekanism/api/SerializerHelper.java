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
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.ChemicalUtils;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

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
     * Codec version of the old CraftingHelper.getItemStack
     */
    public static final Codec<ItemStack> ITEMSTACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          BuiltInRegistries.ITEM.byNameCodec().fieldOf(JsonConstants.ITEM).forGetter(ItemStack::getItem),
          ExtraCodecs.POSITIVE_INT.optionalFieldOf(JsonConstants.COUNT, 1).forGetter(ItemStack::getCount),
          CraftingHelper.TAG_CODEC.optionalFieldOf(JsonConstants.NBT).forGetter(stack -> Optional.ofNullable(stack.getTag()))
    ).apply(instance, ItemStack::new));

    /**
     * Fluid Codec which makes extra sure we don't end up with an empty/invalid fluid
     */
    private static final Codec<Fluid> NON_EMPTY_FLUID_CODEC = ExtraCodecs.validate(BuiltInRegistries.FLUID.byNameCodec(),
          fluid -> fluid == Fluids.EMPTY ? DataResult.error(() -> "Invalid fluid type") : DataResult.success(fluid));

    /**
     * Fluidstack codec to maintain compatibility with our old json
     */
    public static final Codec<FluidStack> FLUIDSTACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          NON_EMPTY_FLUID_CODEC.fieldOf(JsonConstants.FLUID).forGetter(FluidStack::getFluid),
          ExtraCodecs.POSITIVE_INT.fieldOf(JsonConstants.AMOUNT).forGetter(FluidStack::getAmount),
          CraftingHelper.TAG_CODEC.optionalFieldOf(JsonConstants.NBT).forGetter(stack -> Optional.ofNullable(stack.getTag()))
    ).apply(instance, (fluid, amount, tag) -> {
        //Note: We don't use the constructor that accepts a tag to avoid having to copy it
        FluidStack stack = new FluidStack(fluid, amount);
        tag.ifPresent(stack::setTag);
        return stack;
    }));

    /**
     * Codec to get any kind of chemical stack, based on a "chemicalType" field. See also {@link ChemicalType}
     */
    public static final Codec<ChemicalStack<?>> BOXED_CHEMICALSTACK_CODEC = ChemicalType.CODEC.dispatch(JsonConstants.CHEMICAL_TYPE, ChemicalType::getTypeFor, type -> switch (type) {
        case GAS -> ChemicalUtils.GAS_STACK_CODEC;
        case INFUSION -> ChemicalUtils.INFUSION_STACK_CODEC;
        case PIGMENT -> ChemicalUtils.PIGMENT_STACK_CODEC;
        case SLURRY -> ChemicalUtils.SLURRY_STACK_CODEC;
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
    public static <SOURCE, THIS_TYPE> RecordCodecBuilder<SOURCE, Optional<THIS_TYPE>> dependentOptionality(RecordCodecBuilder<SOURCE, ? extends Optional<?>> primaryField, MapCodec<Optional<THIS_TYPE>> dependentCodec, Function<SOURCE, Optional<THIS_TYPE>> dependentGetter) {
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
    public static <SOURCE, THIS_TYPE> RecordCodecBuilder<SOURCE, Optional<THIS_TYPE>> oneRequired(RecordCodecBuilder<SOURCE, ? extends Optional<?>> otherField, MapCodec<Optional<THIS_TYPE>> dependentCodec, Function<SOURCE, Optional<THIS_TYPE>> dependentGetter) {
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