package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackToEnergyRecipeSerializer<RECIPE extends ItemStackToEnergyRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ItemStackToEnergyRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, JsonConstants.INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.INPUT) :
                            JSONUtils.getJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        FloatingLong output = SerializerHelper.getFloatingLong(json, JsonConstants.OUTPUT);
        if (output.isZero()) {
            throw new JsonSyntaxException("Expected output to be greater than zero.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
            FloatingLong output = FloatingLong.readFromBuffer(buffer);
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to energy recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to energy recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ItemStackToEnergyRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient input, FloatingLong output);
    }
}