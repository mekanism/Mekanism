package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer implements RecipeSerializer<BasicRotaryRecipe> {

    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStackIngredient> FLUID_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getFluidInput, JsonConstants.FLUID_INPUT, FluidStackIngredientCreator.INSTANCE.codec());
    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStack> FLUID_OUTPUT_FIELD = RecordCodecBuilder.of(BasicRotaryRecipe::getFluidOutputRaw, JsonConstants.FLUID_OUTPUT, SerializerHelper.FLUIDSTACK_CODEC);
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStackIngredient> GAS_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getGasInput, JsonConstants.GAS_INPUT, GasStackIngredientCreator.INSTANCE.codec());
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStack> GAS_OUTPUT_FIELD = RecordCodecBuilder.of(BasicRotaryRecipe::getGasOutputRaw, JsonConstants.GAS_OUTPUT, ChemicalUtils.GAS_STACK_CODEC);

    private Codec<BasicRotaryRecipe> bothWaysCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_INPUT_FIELD,
              GAS_OUTPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private Codec<BasicRotaryRecipe> fluidToGasCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private Codec<BasicRotaryRecipe> gasToFluidCodec() {
        return RecordCodecBuilder.create(i -> i.group(
              GAS_INPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private final BasicRotaryRecipe.Factory factory;
    private final Lazy<Codec<BasicRotaryRecipe>> codec;

    public RotaryRecipeSerializer(BasicRotaryRecipe.Factory factory) {
        this.factory = factory;
        this.codec = Lazy.of(this::makeCodec);
    }

    private Codec<BasicRotaryRecipe> makeCodec() {
        return new AlternativeMapCodec<>(bothWaysCodec(), new AlternativeMapCodec<>(fluidToGasCodec(), gasToFluidCodec()).codec()).codec();
    }

    @NotNull
    @Override
    public Codec<BasicRotaryRecipe> codec() {
        return this.codec.get();
    }

    @Override
    public BasicRotaryRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient fluidInputIngredient = null;
            GasStackIngredient gasInputIngredient = null;
            GasStack gasOutput = null;
            FluidStack fluidOutput = null;
            boolean hasFluidToGas = buffer.readBoolean();
            if (hasFluidToGas) {
                fluidInputIngredient = IngredientCreatorAccess.fluid().read(buffer);
                gasOutput = GasStack.readFromPacket(buffer);
            }
            boolean hasGasToFluid = buffer.readBoolean();
            if (hasGasToFluid) {
                gasInputIngredient = IngredientCreatorAccess.gas().read(buffer);
                fluidOutput = FluidStack.readFromPacket(buffer);
            }
            if (hasFluidToGas && hasGasToFluid) {
                return this.factory.create(fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
            } else if (hasFluidToGas) {
                return this.factory.create(fluidInputIngredient, gasOutput);
            } else if (hasGasToFluid) {
                return this.factory.create(gasInputIngredient, fluidOutput);
            }
            //Should never happen, but if we somehow get here log it
            Mekanism.logger.error("Error reading rotary recipe from packet. A recipe got sent with no conversion in either direction.");
            return null;
        } catch (Exception e) {
            Mekanism.logger.error("Error reading rotary recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicRotaryRecipe recipe) {
        try {
            buffer.writeBoolean(recipe.hasFluidToGas());
            if (recipe.hasFluidToGas()) {
                recipe.getFluidInput().write(buffer);
                recipe.getGasOutputRaw().writeToPacket(buffer);
            }
            buffer.writeBoolean(recipe.hasGasToFluid());
            if (recipe.hasGasToFluid()) {
                recipe.getGasInput().write(buffer);
                recipe.getFluidOutputRaw().writeToPacket(buffer);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Error writing rotary recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<RECIPE extends BasicRotaryRecipe> {

        RECIPE create(FluidStackIngredient fluidInput, GasStack gasOutput);

        RECIPE create(GasStackIngredient gasInput, FluidStack fluidOutput);

        RECIPE create(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput);
    }

    /**
     * Like a cross between {@link net.neoforged.neoforge.common.util.NeoForgeExtraCodecs#withAlternative(Codec, Codec)} and
     * {@link net.neoforged.neoforge.common.util.NeoForgeExtraCodecs#mapWithAlternative(MapCodec, MapCodec)} so that the recipe dispatch codec can inline our stuff.
     */
    private static class AlternativeMapCodec<TYPE> extends MapCodec<TYPE> {

        private final MapCodec<TYPE> codec;
        private final MapCodec<TYPE> alternative;

        public AlternativeMapCodec(Codec<TYPE> codec, Codec<TYPE> alternative) {
            this.codec = ((MapCodecCodec<TYPE>) codec).codec();
            this.alternative = ((MapCodecCodec<TYPE>) alternative).codec();
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(codec.keys(ops), alternative.keys(ops));
        }

        @Override
        public <T> DataResult<TYPE> decode(DynamicOps<T> ops, MapLike<T> input) {
            DataResult<TYPE> result = codec.decode(ops, input);
            if (result.error().isEmpty()) {
                return result;
            }
            return alternative.decode(ops, input);
        }

        @Override
        public <T> RecordBuilder<T> encode(TYPE input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            try {
                //Note: This encode call causes an error when just a single one is taking place
                RecordBuilder<T> encoded = codec.encode(input, ops, prefix);
                //Build it to see if there is an error
                DataResult<T> result = encoded.build((T) null);
                if (result.error().isEmpty()) {
                    //But then we even if there isn't we have to encode it again so that we can actually allow the building to apply
                    return codec.encode(input, ops, prefix);
                }
            } catch (Exception ignored) {
            }
            return alternative.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "AlternativeMapCodec[" + codec + ", " + alternative + "]";
        }
    }
}