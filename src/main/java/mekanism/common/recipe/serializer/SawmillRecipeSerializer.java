package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SawmillRecipeSerializer<T extends SawmillRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public SawmillRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") :
                            JSONUtils.getJsonObject(json, "input");
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        ItemStack mainOutput = ItemStack.EMPTY;
        ItemStack secondaryOutput = ItemStack.EMPTY;
        double secondaryChance = 0;
        if (json.has("secondaryOutput") || json.has("secondaryChance")) {
            if (json.has("mainOutput")) {
                //Allow for the main output to be optional if we have a secondary output
                mainOutput = SerializerHelper.getItemStack(json, "mainOutput");
            }
            //If we have either json element for secondary information, assume we have both and fail if we can't get one of them
            JsonElement chance = json.get("secondaryChance");
            if (!JSONUtils.isNumber(chance)) {
                throw new JsonSyntaxException("Expected secondaryChance to be a number greater than zero.");
            }
            secondaryChance = chance.getAsJsonPrimitive().getAsDouble();
            if (secondaryChance <= 0) {
                throw new JsonSyntaxException("Expected secondaryChance to be greater than zero.");
            }
            secondaryOutput = SerializerHelper.getItemStack(json, "secondaryOutput");
        } else {
            //If we don't have a secondary output require a main output
            mainOutput = SerializerHelper.getItemStack(json, "mainOutput");
        }
        return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
        ItemStack mainOutput = buffer.readItemStack();
        ItemStack secondaryOutput = buffer.readItemStack();
        double secondaryChance = buffer.readDouble();
        return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends SawmillRecipe> {

        T create(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance);
    }
}