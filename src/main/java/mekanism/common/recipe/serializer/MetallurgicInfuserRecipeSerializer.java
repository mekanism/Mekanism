package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MetallurgicInfuserRecipeSerializer<T extends MetallurgicInfuserRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public MetallurgicInfuserRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = JSONUtils.isJsonArray(json, "itemInput") ? JSONUtils.getJsonArray(json, "itemInput") :
                                JSONUtils.getJsonObject(json, "itemInput");
        ItemStackIngredient itemIngredient = ItemStackIngredient.deserialize(itemInput);
        JsonElement infusionInput = JSONUtils.isJsonArray(json, "infusionInput") ? JSONUtils.getJsonArray(json, "infusionInput") :
                                    JSONUtils.getJsonObject(json, "infusionInput");
        InfusionIngredient infusionIngredient = InfusionIngredient.deserialize(infusionInput);
        ItemStack output = SerializerHelper.getItemStack(json, "output");
        return this.factory.create(recipeId, itemIngredient, infusionIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient itemInput = ItemStackIngredient.read(buffer);
        InfusionIngredient infusionInput = InfusionIngredient.read(buffer);
        ItemStack output = buffer.readItemStack();
        return this.factory.create(recipeId, itemInput, infusionInput, output);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends MetallurgicInfuserRecipe> {

        T create(ResourceLocation id, ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output);
    }
}