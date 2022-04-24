package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ElectrolysisRecipeSerializer<RECIPE extends ElectrolysisRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ElectrolysisRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().deserialize(input);
        GasStack leftGasOutput = SerializerHelper.getGasStack(json, JsonConstants.LEFT_GAS_OUTPUT);
        GasStack rightGasOutput = SerializerHelper.getGasStack(json, JsonConstants.RIGHT_GAS_OUTPUT);
        FloatingLong energyMultiplier = FloatingLong.ONE;
        if (json.has(JsonConstants.ENERGY_MULTIPLIER)) {
            energyMultiplier = SerializerHelper.getFloatingLong(json, JsonConstants.ENERGY_MULTIPLIER);
            if (energyMultiplier.smallerThan(FloatingLong.ONE)) {
                throw new JsonSyntaxException("Expected energyMultiplier to be at least one.");
            }
        }
        if (leftGasOutput.isEmpty() || rightGasOutput.isEmpty()) {
            throw new JsonSyntaxException("Electrolysis recipe outputs must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient input = IngredientCreatorAccess.fluid().read(buffer);
            FloatingLong energyMultiplier = FloatingLong.readFromBuffer(buffer);
            GasStack leftGasOutput = GasStack.readFromPacket(buffer);
            GasStack rightGasOutput = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, input, energyMultiplier, leftGasOutput, rightGasOutput);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading electrolysis recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing electrolysis recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ElectrolysisRecipe> {

        RECIPE create(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput);
    }
}