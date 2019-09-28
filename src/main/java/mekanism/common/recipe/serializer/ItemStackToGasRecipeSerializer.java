package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.impl.ItemStackToGasIRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ItemStackToGasRecipeSerializer<T extends ItemStackToGasIRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ItemStackToGasRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") :
                            JSONUtils.getJsonObject(json, "input");
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        //TODO
        Gas outputGas = MekanismAPI.EMPTY_GAS;
        int outputGasAmount = 0;
        return this.factory.create(recipeId, inputIngredient, outputGas, outputGasAmount);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
        Gas outputGas = buffer.readRegistryId();
        int outputGasAmount = buffer.readInt();
        return this.factory.create(recipeId, inputIngredient, outputGas, outputGasAmount);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends ItemStackToGasIRecipe> {

        T create(ResourceLocation id, ItemStackIngredient input, Gas outputGas, int outputGasAmount);
    }
}