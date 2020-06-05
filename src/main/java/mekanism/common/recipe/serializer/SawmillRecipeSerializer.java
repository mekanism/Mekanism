package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SawmillRecipeSerializer<RECIPE extends SawmillRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public SawmillRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, JsonConstants.INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.INPUT) :
                            JSONUtils.getJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = ItemStackIngredient.deserialize(input);
        ItemStack mainOutput = ItemStack.EMPTY;
        ItemStack secondaryOutput = ItemStack.EMPTY;
        double secondaryChance = 0;
        if (json.has(JsonConstants.SECONDARY_OUTPUT) || json.has(JsonConstants.SECONDARY_CHANCE)) {
            if (json.has(JsonConstants.MAIN_OUTPUT)) {
                //Allow for the main output to be optional if we have a secondary output
                mainOutput = SerializerHelper.getItemStack(json, JsonConstants.MAIN_OUTPUT);
                if (mainOutput.isEmpty()) {
                    throw new JsonSyntaxException("Sawmill main recipe output must not be empty, if it is defined.");
                }
            }
            //If we have either json element for secondary information, assume we have both and fail if we can't get one of them
            JsonElement chance = json.get(JsonConstants.SECONDARY_CHANCE);
            if (!JSONUtils.isNumber(chance)) {
                throw new JsonSyntaxException("Expected secondaryChance to be a number greater than zero.");
            }
            secondaryChance = chance.getAsJsonPrimitive().getAsDouble();
            if (secondaryChance <= 0 || secondaryChance > 1) {
                throw new JsonSyntaxException("Expected secondaryChance to be greater than zero, and less than or equal to one.");
            }
            secondaryOutput = SerializerHelper.getItemStack(json, JsonConstants.SECONDARY_OUTPUT);
            if (secondaryOutput.isEmpty()) {
                throw new JsonSyntaxException("Sawmill secondary recipe output must not be empty, if there is no main output.");
            }
        } else {
            //If we don't have a secondary output require a main output
            mainOutput = SerializerHelper.getItemStack(json, JsonConstants.MAIN_OUTPUT);
            if (mainOutput.isEmpty()) {
                throw new JsonSyntaxException("Sawmill main recipe output must not be empty, if there is no secondary output.");
            }
        }
        return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
    }

    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputIngredient = ItemStackIngredient.read(buffer);
            ItemStack mainOutput = buffer.readItemStack();
            ItemStack secondaryOutput = buffer.readItemStack();
            double secondaryChance = buffer.readDouble();
            return this.factory.create(recipeId, inputIngredient, mainOutput, secondaryOutput, secondaryChance);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading sawmill recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing sawmill recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends SawmillRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance);
    }
}