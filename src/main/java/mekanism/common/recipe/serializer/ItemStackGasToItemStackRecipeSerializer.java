package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackGasToItemStackRecipeSerializer<T extends ItemStackGasToItemStackRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ItemStackGasToItemStackRecipeSerializer(IFactory<T> factory) {
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
        ItemStack output = SerializerHelper.getItemStack(json, "output");
        return this.factory.create(recipeId, itemIngredient, gasIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient itemInput = ItemStackIngredient.read(buffer);
            GasStackIngredient gasInput = GasStackIngredient.read(buffer);
            ItemStack output = buffer.readItemStack();
            return this.factory.create(recipeId, itemInput, gasInput, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack gas to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack gas to itemstack recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends ItemStackGasToItemStackRecipe> {

        T create(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output);
    }
}