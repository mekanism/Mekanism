package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalDissolutionRecipeSerializer<RECIPE extends ChemicalDissolutionRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ChemicalDissolutionRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        JsonElement itemInput = GsonHelper.isArrayNode(json, JsonConstants.ITEM_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.ITEM_INPUT) :
                                GsonHelper.getAsJsonObject(json, JsonConstants.ITEM_INPUT);
        ItemStackIngredient itemIngredient = IngredientCreatorAccess.item().deserialize(itemInput);
        JsonElement gasInput = GsonHelper.isArrayNode(json, JsonConstants.GAS_INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.GAS_INPUT) :
                               GsonHelper.getAsJsonObject(json, JsonConstants.GAS_INPUT);
        GasStackIngredient gasIngredient = IngredientCreatorAccess.gas().deserialize(gasInput);
        ChemicalStack<?> output = SerializerHelper.getBoxedChemicalStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, itemIngredient, gasIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient itemInput = IngredientCreatorAccess.item().read(buffer);
            GasStackIngredient gasInput = IngredientCreatorAccess.gas().read(buffer);
            ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
            ChemicalStack<?> output = switch (chemicalType) {
                case GAS -> GasStack.readFromPacket(buffer);
                case INFUSION -> InfusionStack.readFromPacket(buffer);
                case PIGMENT -> PigmentStack.readFromPacket(buffer);
                case SLURRY -> SlurryStack.readFromPacket(buffer);
            };
            return this.factory.create(recipeId, itemInput, gasInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack gas to gas recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack gas to gas recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ChemicalDissolutionRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output);
    }
}