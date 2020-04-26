package mekanism.common.recipe.serializer;

import javax.annotation.Nonnull;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class NucleosynthesizingRecipeSerializer<T extends NucleosynthesizingRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public NucleosynthesizingRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = JSONUtils.isJsonArray(json, JsonConstants.ITEM_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.ITEM_INPUT) :
                                JSONUtils.getJsonObject(json, JsonConstants.ITEM_INPUT);
        ItemStackIngredient itemIngredient = ItemStackIngredient.deserialize(itemInput);
        JsonElement gasInput = JSONUtils.isJsonArray(json, JsonConstants.GAS_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.GAS_INPUT) :
                               JSONUtils.getJsonObject(json, JsonConstants.GAS_INPUT);
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(gasInput);

        int duration;
        JsonElement ticks = json.get(JsonConstants.DURATION);
        if (!JSONUtils.isNumber(ticks)) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        duration = ticks.getAsJsonPrimitive().getAsInt();
        if (duration <= 0) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        ItemStack itemOutput = SerializerHelper.getItemStack(json, JsonConstants.OUTPUT);
        if (itemOutput.isEmpty()) {
            throw new JsonSyntaxException("Nucleosynthesizing item output must not be empty.");
        }
        return this.factory.create(recipeId, itemIngredient, gasIngredient, itemOutput, duration);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputSolid = ItemStackIngredient.read(buffer);
            GasStackIngredient inputGas = GasStackIngredient.read(buffer);
            ItemStack outputItem = buffer.readItemStack();
            int duration = buffer.readVarInt();
            return this.factory.create(recipeId, inputSolid, inputGas, outputItem, duration);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading nucleosynthesizing recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing nucleosynthesizing recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends NucleosynthesizingRecipe> {

        T create(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack outputItem, int duration);
    }
}