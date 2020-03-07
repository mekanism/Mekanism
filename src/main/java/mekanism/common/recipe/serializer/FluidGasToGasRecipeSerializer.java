package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class FluidGasToGasRecipeSerializer<T extends FluidGasToGasRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public FluidGasToGasRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement fluidInput = JSONUtils.isJsonArray(json, JsonConstants.FLUID_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.FLUID_INPUT) :
                                 JSONUtils.getJsonObject(json, JsonConstants.FLUID_INPUT);
        FluidStackIngredient fluidIngredient = FluidStackIngredient.deserialize(fluidInput);
        JsonElement gasInput = JSONUtils.isJsonArray(json, JsonConstants.GAS_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.GAS_INPUT) :
                               JSONUtils.getJsonObject(json, JsonConstants.GAS_INPUT);
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(gasInput);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, fluidIngredient, gasIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            FluidStackIngredient fluidInput = FluidStackIngredient.read(buffer);
            GasStackIngredient gasInput = GasStackIngredient.read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, fluidInput, gasInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading fluid gas to gas recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing fluid gas to gas recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends FluidGasToGasRecipe> {

        T create(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack output);
    }
}