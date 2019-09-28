package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
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
        //TODO
        Gas outputGas = MekanismAPI.EMPTY_GAS;
        int outputGasAmount = 0;
        return this.factory.create(recipeId, itemIngredient, gasIngredient, outputGas, outputGasAmount);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient itemInput = ItemStackIngredient.read(buffer);
        GasStackIngredient gasInput = GasStackIngredient.read(buffer);
        Gas outputGas = buffer.readRegistryId();
        int outputGasAmount = buffer.readInt();
        return this.factory.create(recipeId, itemInput, gasInput, outputGas, outputGasAmount);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends ItemStackGasToGasRecipe> {

        T create(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, Gas outputGas, int outputGasAmount);
    }
}