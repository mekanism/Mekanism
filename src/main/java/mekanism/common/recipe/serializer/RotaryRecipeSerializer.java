package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RotaryRecipeSerializer<RECIPE extends RotaryRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public RotaryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        FluidStackIngredient fluidInputIngredient = null;
        GasStackIngredient gasInputIngredient = null;
        GasStack gasOutput = null;
        FluidStack fluidOutput = null;
        boolean hasFluidToGas = false;
        boolean hasGasToFluid = false;
        if (json.has(JsonConstants.FLUID_INPUT) || json.has(JsonConstants.GAS_OUTPUT)) {
            JsonElement fluidInput = JSONUtils.isJsonArray(json, JsonConstants.FLUID_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.FLUID_INPUT) :
                                     JSONUtils.getJsonObject(json, JsonConstants.FLUID_INPUT);
            fluidInputIngredient = FluidStackIngredient.deserialize(fluidInput);
            gasOutput = SerializerHelper.getGasStack(json, JsonConstants.GAS_OUTPUT);
            hasFluidToGas = true;
            if (gasOutput.isEmpty()) {
                throw new JsonSyntaxException("Rotary recipe gas output cannot be empty if it is defined.");
            }
        }
        if (json.has(JsonConstants.GAS_INPUT) || json.has(JsonConstants.FLUID_OUTPUT)) {
            JsonElement gasInput = JSONUtils.isJsonArray(json, JsonConstants.GAS_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.GAS_INPUT) :
                                   JSONUtils.getJsonObject(json, JsonConstants.GAS_INPUT);
            gasInputIngredient = GasStackIngredient.deserialize(gasInput);
            fluidOutput = SerializerHelper.getFluidStack(json, JsonConstants.FLUID_OUTPUT);
            hasGasToFluid = true;
            if (fluidOutput.isEmpty()) {
                throw new JsonSyntaxException("Rotary recipe fluid output cannot be empty if it is defined.");
            }
        }
        if (hasFluidToGas && hasGasToFluid) {
            return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
        } else if (hasFluidToGas) {
            return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
        } else if (hasGasToFluid) {
            return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
        }
        throw new JsonSyntaxException("Rotary recipes require at least a gas to fluid or fluid to gas conversion.");
    }

    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            FluidStackIngredient fluidInputIngredient = null;
            GasStackIngredient gasInputIngredient = null;
            GasStack gasOutput = null;
            FluidStack fluidOutput = null;
            boolean hasFluidToGas = buffer.readBoolean();
            if (hasFluidToGas) {
                fluidInputIngredient = FluidStackIngredient.read(buffer);
                gasOutput = GasStack.readFromPacket(buffer);
            }
            boolean hasGasToFluid = buffer.readBoolean();
            if (hasGasToFluid) {
                gasInputIngredient = GasStackIngredient.read(buffer);
                fluidOutput = FluidStack.readFromPacket(buffer);
            }
            if (hasFluidToGas && hasGasToFluid) {
                return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
            } else if (hasFluidToGas) {
                return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
            } else if (hasGasToFluid) {
                return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
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
    public void write(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        if (recipe.hasFluidToGas() || recipe.hasGasToFluid()) {
            try {
                recipe.write(buffer);
            } catch (Exception e) {
                Mekanism.logger.error("Error writing rotary recipe to packet.", e);
                throw e;
            }
        } else {
            Mekanism.logger.error("Error writing rotary recipe to packet. {} has no conversion in either direction, so was not sent.", recipe);
        }
    }

    public interface IFactory<RECIPE extends RotaryRecipe> {

        RECIPE create(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput);

        RECIPE create(ResourceLocation id, GasStackIngredient gasInput, FluidStack fluidOutput);

        RECIPE create(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput);
    }
}