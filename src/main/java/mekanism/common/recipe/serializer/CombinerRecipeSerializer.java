package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CombinerRecipeSerializer<RECIPE extends CombinerRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public CombinerRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement mainInput = JSONUtils.isArrayNode(json, JsonConstants.MAIN_INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.MAIN_INPUT) :
                                JSONUtils.getAsJsonObject(json, JsonConstants.MAIN_INPUT);
        ItemStackIngredient mainIngredient = ItemStackIngredient.deserialize(mainInput);
        JsonElement extraInput = JSONUtils.isArrayNode(json, JsonConstants.EXTRA_INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.EXTRA_INPUT) :
                                 JSONUtils.getAsJsonObject(json, JsonConstants.EXTRA_INPUT);
        ItemStackIngredient extraIngredient = ItemStackIngredient.deserialize(extraInput);
        ItemStack output = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Combiner recipe output must not be empty.");
        }
        return this.factory.create(recipeId, mainIngredient, extraIngredient, output);
    }

    @Override
    public RECIPE fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient mainInput = ItemStackIngredient.read(buffer);
            ItemStackIngredient extraInput = ItemStackIngredient.read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(recipeId, mainInput, extraInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading combiner recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
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