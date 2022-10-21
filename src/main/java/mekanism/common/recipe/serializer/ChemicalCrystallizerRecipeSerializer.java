package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer<RECIPE extends ChemicalCrystallizerRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ChemicalCrystallizerRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ChemicalType chemicalType = SerializerHelper.getChemicalType(json);
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).deserialize(input);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
            ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading boxed chemical to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing boxed chemical to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ChemicalCrystallizerRecipe> {

        RECIPE create(ResourceLocation id, ChemicalStackIngredient<?, ?> input, ItemStack output);
    }
}