package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ItemStackChemicalToItemStackRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

    protected ItemStackChemicalToItemStackRecipeSerializer(IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
        this.factory = factory;
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = GsonHelper.isArrayNode(json, JsonConstants.ITEM_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.ITEM_INPUT) :
                                GsonHelper.getAsJsonObject(json, JsonConstants.ITEM_INPUT);
        ItemStackIngredient itemIngredient = IngredientCreatorAccess.item().deserialize(itemInput);
        JsonElement chemicalInput = GsonHelper.isArrayNode(json, JsonConstants.CHEMICAL_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.CHEMICAL_INPUT) :
                                    GsonHelper.getAsJsonObject(json, JsonConstants.CHEMICAL_INPUT);
        INGREDIENT chemicalIngredient = getDeserializer().deserialize(chemicalInput);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, itemIngredient, chemicalIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
            INGREDIENT chemicalInput = getDeserializer().read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(recipeId, itemInput, chemicalInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack chemical to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack chemical to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>> {

        RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);
    }
}