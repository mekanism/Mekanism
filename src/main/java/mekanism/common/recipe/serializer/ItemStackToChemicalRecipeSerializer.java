package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ItemStackToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, RECIPE> factory;

    protected ItemStackToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, RECIPE> factory) {
        this.factory = factory;
    }

    protected abstract STACK fromJson(@Nonnull JsonObject json, @Nonnull String key);

    protected abstract STACK fromBuffer(@Nonnull FriendlyByteBuf buffer);

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
        STACK output = fromJson(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            STACK output = fromBuffer(buffer);
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to chemical recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to chemical recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> {

        RECIPE create(ResourceLocation id, ItemStackIngredient input, STACK output);
    }
}