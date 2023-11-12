package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import mekanism.common.recipe.impl.RotaryIRecipe;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class RotaryRecipeSerializer implements RecipeSerializer<BasicRotaryRecipe> {

    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStackIngredient> FLUID_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getFluidInput, JsonConstants.FLUID_INPUT, FluidStackIngredientCreator.INSTANCE.codec());
    private final RecordCodecBuilder<BasicRotaryRecipe, FluidStack> FLUID_OUTPUT_FIELD = RecordCodecBuilder.of(BasicRotaryRecipe::getFluidOutputRaw, JsonConstants.FLUID_OUTPUT, SerializerHelper.FLUIDSTACK_CODEC);
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStackIngredient> GAS_INPUT_FIELD = RecordCodecBuilder.of(RotaryRecipe::getGasInput, JsonConstants.GAS_INPUT, GasStackIngredientCreator.INSTANCE.codec());
    private final RecordCodecBuilder<BasicRotaryRecipe, GasStack> GAS_OUTPUT_FIELD = RecordCodecBuilder.of(BasicRotaryRecipe::getGasOutputRaw, JsonConstants.GAS_INPUT, ChemicalUtils.GAS_STACK_CODEC);

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

    private final IFactory<BasicRotaryRecipe> factory;
    private final Lazy<Codec<BasicRotaryRecipe>> codec;

    public RotaryRecipeSerializer(IFactory<BasicRotaryRecipe> factory) {
        this.factory = factory;
        this.codec = Lazy.of(this::makeCodec);
    }

    private Codec<BasicRotaryRecipe> makeCodec() {
        return NeoForgeExtraCodecs.withAlternative(bothWaysCodec(), NeoForgeExtraCodecs.withAlternative(fluidToGasCodec(), gasToFluidCodec()));
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
}