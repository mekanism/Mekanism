package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackToEnergyRecipeSerializer<T extends ItemStackToEnergyRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ItemStackToEnergyRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, JsonConstants.INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.INPUT) :
                            JSONUtils.getJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        double output;
        if (!json.has(JsonConstants.OUTPUT)) {
            throw new JsonSyntaxException("Missing '" + JsonConstants.OUTPUT + "', expected to find a double");
        }
        JsonElement energy = json.get(JsonConstants.OUTPUT);
        if (!JSONUtils.isNumber(energy)) {
            throw new JsonSyntaxException("Expected output to be a double greater than zero.");
        }
        output = energy.getAsJsonPrimitive().getAsDouble();
        if (output <= 0) {
            throw new JsonSyntaxException("Expected output to be greater than zero.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
            double output = buffer.readDouble();
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to energy recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to energy recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends ItemStackToEnergyRecipe> {

        T create(ResourceLocation id, ItemStackIngredient input, double output);
    }
}