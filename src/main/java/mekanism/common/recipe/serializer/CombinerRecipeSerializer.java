package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.impl.CombinerIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CombinerRecipeSerializer<T extends CombinerIRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public CombinerRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement mainInput = JSONUtils.isJsonArray(json, "mainInput") ? JSONUtils.getJsonArray(json, "mainInput") :
                                JSONUtils.getJsonObject(json, "mainInput");
        ItemStackIngredient mainIngredient = ItemStackIngredient.deserialize(mainInput);
        JsonElement extraInput = JSONUtils.isJsonArray(json, "extraInput") ? JSONUtils.getJsonArray(json, "extraInput") :
                                 JSONUtils.getJsonObject(json, "extraInput");
        ItemStackIngredient extraIngredient = ItemStackIngredient.deserialize(extraInput);
        ItemStack output = SerializerHelper.getItemStack(json, "output");
        return this.factory.create(recipeId, mainIngredient, extraIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient mainInput = ItemStackIngredient.read(buffer);
        ItemStackIngredient extraInput = ItemStackIngredient.read(buffer);
        ItemStack output = buffer.readItemStack();
        return this.factory.create(recipeId, mainInput, extraInput, output);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends CombinerIRecipe> {

        T create(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output);
    }
}