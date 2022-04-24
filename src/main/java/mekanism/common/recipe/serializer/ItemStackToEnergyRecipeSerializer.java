package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackToEnergyRecipeSerializer<RECIPE extends ItemStackToEnergyRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ItemStackToEnergyRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
        FloatingLong output = SerializerHelper.getFloatingLong(json, JsonConstants.OUTPUT);
        if (output.isZero()) {
            throw new JsonSyntaxException("Expected output to be greater than zero.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            FloatingLong output = FloatingLong.readFromBuffer(buffer);
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to energy recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to energy recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ItemStackToEnergyRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient input, FloatingLong output);
    }
}