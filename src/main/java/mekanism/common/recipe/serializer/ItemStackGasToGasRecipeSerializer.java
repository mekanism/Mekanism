package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackGasToGasRecipeSerializer<T extends ItemStackGasToGasRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ItemStackGasToGasRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = JSONUtils.isJsonArray(json, "itemInput") ? JSONUtils.getJsonArray(json, "itemInput") :
                                JSONUtils.getJsonObject(json, "itemInput");
        ItemStackIngredient itemIngredient = ItemStackIngredient.deserialize(itemInput);
        JsonElement gasInput = JSONUtils.isJsonArray(json, "gasInput") ? JSONUtils.getJsonArray(json, "gasInput") :
                               JSONUtils.getJsonObject(json, "gasInput");
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(gasInput);
        GasStack output = SerializerHelper.getGasStack(json, "output");
        return this.factory.create(recipeId, itemIngredient, gasIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient itemInput = ItemStackIngredient.read(buffer);
            GasStackIngredient gasInput = GasStackIngredient.read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, itemInput, gasInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack gas to gas recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack gas to gas recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends ItemStackGasToGasRecipe> {

        T create(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output);
    }
}