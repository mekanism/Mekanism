package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class ItemStackToChemicalRecipeSerializer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      T extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<CHEMICAL, STACK, T> factory;

    public ItemStackToChemicalRecipeSerializer(IFactory<CHEMICAL, STACK, T> factory) {
        this.factory = factory;
    }

    protected abstract STACK fromJson(@Nonnull JsonObject json, @Nonnull String key);

    protected abstract STACK fromBuffer(@Nonnull PacketBuffer buffer);

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") :
                            JSONUtils.getJsonObject(json, "input");
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        STACK output = fromJson(json, "output");
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
            STACK output = fromBuffer(buffer);
            return this.factory.create(recipeId, inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading itemstack to chemical recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing itemstack to chemical recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, T extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> {

        T create(ResourceLocation id, ItemStackIngredient input, STACK output);
    }
}