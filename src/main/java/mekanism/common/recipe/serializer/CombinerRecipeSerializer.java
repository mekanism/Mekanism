package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CombinerRecipeSerializer<RECIPE extends CombinerRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public CombinerRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement mainInput = GsonHelper.isArrayNode(json, JsonConstants.MAIN_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.MAIN_INPUT) :
                                GsonHelper.getAsJsonObject(json, JsonConstants.MAIN_INPUT);
        ItemStackIngredient mainIngredient = IngredientCreatorAccess.item().deserialize(mainInput);
        JsonElement extraInput = GsonHelper.isArrayNode(json, JsonConstants.EXTRA_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.EXTRA_INPUT) :
                                 GsonHelper.getAsJsonObject(json, JsonConstants.EXTRA_INPUT);
        ItemStackIngredient extraIngredient = IngredientCreatorAccess.item().deserialize(extraInput);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Combiner recipe output must not be empty.");
        }
        return this.factory.create(recipeId, mainIngredient, extraIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient mainInput = IngredientCreatorAccess.item().read(buffer);
            ItemStackIngredient extraInput = IngredientCreatorAccess.item().read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(recipeId, mainInput, extraInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading combiner recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing combiner recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends CombinerRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output);
    }
}