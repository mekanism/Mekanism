package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ChemicalChemicalToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

    protected ChemicalChemicalToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
        this.factory = factory;
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    protected abstract STACK fromJson(@Nonnull JsonObject json, @Nonnull String key);

    protected abstract STACK fromBuffer(@Nonnull FriendlyByteBuf buffer);

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement leftIngredients = GsonHelper.isArrayNode(json, JsonConstants.LEFT_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.LEFT_INPUT) :
                                      GsonHelper.getAsJsonObject(json, JsonConstants.LEFT_INPUT);
        INGREDIENT leftInput = getDeserializer().deserialize(leftIngredients);
        JsonElement rightIngredients = GsonHelper.isArrayNode(json, JsonConstants.RIGHT_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.RIGHT_INPUT) :
                                       GsonHelper.getAsJsonObject(json, JsonConstants.RIGHT_INPUT);
        INGREDIENT rightInput = getDeserializer().deserialize(rightIngredients);
        STACK output = fromJson(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, leftInput, rightInput, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        try {
            INGREDIENT leftInput = getDeserializer().read(buffer);
            INGREDIENT rightInput = getDeserializer().read(buffer);
            STACK output = fromBuffer(buffer);
            return this.factory.create(recipeId, leftInput, rightInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading chemical chemical to chemical recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing chemical chemical to chemical recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> {

        RECIPE create(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output);
    }
}