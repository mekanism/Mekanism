package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ChemicalChemicalToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

    protected ChemicalChemicalToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
        this.factory = factory;
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    protected abstract STACK fromJson(@Nonnull JsonObject json, @Nonnull String key);

    protected abstract STACK fromBuffer(@Nonnull PacketBuffer buffer);

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement leftIngredients = JSONUtils.isArrayNode(json, JsonConstants.LEFT_INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.LEFT_INPUT) :
                                      JSONUtils.getAsJsonObject(json, JsonConstants.LEFT_INPUT);
        INGREDIENT leftInput = getDeserializer().deserialize(leftIngredients);
        JsonElement rightIngredients = JSONUtils.isArrayNode(json, JsonConstants.RIGHT_INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.RIGHT_INPUT) :
                                       JSONUtils.getAsJsonObject(json, JsonConstants.RIGHT_INPUT);
        INGREDIENT rightInput = getDeserializer().deserialize(rightIngredients);
        STACK output = fromJson(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, leftInput, rightInput, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
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
    public void toNetwork(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing chemical chemical to chemical recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> {

        RECIPE create(ResourceLocation id, INGREDIENT leftInput, INGREDIENT rightInput, STACK output);
    }
}