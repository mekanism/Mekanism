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
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ItemStackChemicalToItemStackRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory;

    protected ItemStackChemicalToItemStackRecipeSerializer(IFactory<CHEMICAL, STACK, INGREDIENT, RECIPE> factory) {
        this.factory = factory;
    }

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    //TODO - 1.18: Inline this and move the other overrides over to just using chemical input as the key
    protected String getChemicalInputJsonKey() {
        return JsonConstants.CHEMICAL_INPUT;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = JSONUtils.isArrayNode(json, JsonConstants.ITEM_INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.ITEM_INPUT) :
                                JSONUtils.getAsJsonObject(json, JsonConstants.ITEM_INPUT);
        ItemStackIngredient itemIngredient = ItemStackIngredient.deserialize(itemInput);
        String chemicalInputKey = getChemicalInputJsonKey();
        JsonElement chemicalInput = JSONUtils.isArrayNode(json, chemicalInputKey) ? JSONUtils.getAsJsonArray(json, chemicalInputKey) :
                                    JSONUtils.getAsJsonObject(json, chemicalInputKey);
        INGREDIENT chemicalIngredient = getDeserializer().deserialize(chemicalInput);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, itemIngredient, chemicalIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient itemInput = ItemStackIngredient.read(buffer);
            INGREDIENT chemicalInput = getDeserializer().read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(recipeId, itemInput, chemicalInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack chemical to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack chemical to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>> {

        RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, INGREDIENT chemicalInput, ItemStack output);
    }
}