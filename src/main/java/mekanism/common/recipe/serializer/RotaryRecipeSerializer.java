package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RotaryRecipeSerializer<RECIPE extends RotaryRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public RotaryRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        FluidStackIngredient fluidInputIngredient = null;
        GasStackIngredient gasInputIngredient = null;
        GasStack gasOutput = null;
        FluidStack fluidOutput = null;
        boolean hasFluidToGas = false;
        boolean hasGasToFluid = false;
        if (json.has(JsonConstants.FLUID_INPUT) || json.has(JsonConstants.GAS_OUTPUT)) {
            JsonElement fluidInput = GsonHelper.isArrayNode(json, JsonConstants.FLUID_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.FLUID_INPUT) :
                                     GsonHelper.getAsJsonObject(json, JsonConstants.FLUID_INPUT);
            fluidInputIngredient = IngredientCreatorAccess.fluid().deserialize(fluidInput);
            gasOutput = SerializerHelper.getGasStack(json, JsonConstants.GAS_OUTPUT);
            hasFluidToGas = true;
            if (gasOutput.isEmpty()) {
                throw new JsonSyntaxException("Rotary recipe gas output cannot be empty if it is defined.");
            }
        }
        if (json.has(JsonConstants.GAS_INPUT) || json.has(JsonConstants.FLUID_OUTPUT)) {
            JsonElement gasInput = GsonHelper.isArrayNode(json, JsonConstants.GAS_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.GAS_INPUT) :
                                   GsonHelper.getAsJsonObject(json, JsonConstants.GAS_INPUT);
            gasInputIngredient = IngredientCreatorAccess.gas().deserialize(gasInput);
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
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
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
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
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