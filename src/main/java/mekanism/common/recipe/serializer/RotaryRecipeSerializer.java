package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.basic.BasicRotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer implements RecipeSerializer<BasicRotaryRecipe> {

    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStackIngredient> FLUID_INPUT_FIELD = ExtraCodecs.validate(
          FluidStackIngredientCreator.INSTANCE.codec(),
          ingredient -> ingredient == null ? DataResult.error(() -> "Fluid input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(JsonConstants.FLUID_INPUT).forGetter(BasicRotaryRecipe::getFluidInputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStack> FLUID_OUTPUT_FIELD = ExtraCodecs.validate(
          SerializerHelper.FLUIDSTACK_CODEC,
          stack -> stack.isEmpty() ? DataResult.error(() -> "Fluid output may not be empty") : DataResult.success(stack)
    ).fieldOf(JsonConstants.FLUID_OUTPUT).forGetter(BasicRotaryRecipe::getFluidOutputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStackIngredient> GAS_INPUT_FIELD = ExtraCodecs.validate(
          GasStackIngredientCreator.INSTANCE.codec(),
          ingredient -> ingredient == null ? DataResult.error(() -> "Gas input may not be empty") : DataResult.success(ingredient)
    ).fieldOf(JsonConstants.GAS_INPUT).forGetter(BasicRotaryRecipe::getGasInputRaw);
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStack> GAS_OUTPUT_FIELD = ExtraCodecs.validate(
          ChemicalUtils.GAS_STACK_CODEC,
          stack -> stack.isEmpty() ? DataResult.error(() -> "Gas output may not be empty") : DataResult.success(stack)
    ).fieldOf(JsonConstants.GAS_OUTPUT).forGetter(BasicRotaryRecipe::getGasOutputRaw);

    private MapCodec<BasicRotaryRecipe> bothWaysCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_INPUT_FIELD,
              GAS_OUTPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private MapCodec<BasicRotaryRecipe> fluidToGasCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
              FLUID_INPUT_FIELD,
              GAS_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private MapCodec<BasicRotaryRecipe> gasToFluidCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
              GAS_INPUT_FIELD,
              FLUID_OUTPUT_FIELD
        ).apply(i, this.factory::create));
    }

    private final BasicRotaryRecipe.Factory factory;
    private Codec<BasicRotaryRecipe> codec;

    public RotaryRecipeSerializer(BasicRotaryRecipe.Factory factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<BasicRotaryRecipe> codec() {
        if (codec == null) {
            codec = NeoForgeExtraCodecs.withAlternative(bothWaysCodec(), NeoForgeExtraCodecs.withAlternative(fluidToGasCodec(), gasToFluidCodec())).codec();
        }
        return codec;
    }

    @NotNull
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
            //Should never happen, but if we somehow get here log it and error
            throw new IllegalStateException("A recipe got sent with no conversion in either direction.");
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
}