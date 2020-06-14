package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PressurizedReactionRecipeSerializer<RECIPE extends PressurizedReactionRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public PressurizedReactionRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement itemInput = JSONUtils.isJsonArray(json, JsonConstants.ITEM_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.ITEM_INPUT) :
                                JSONUtils.getJsonObject(json, JsonConstants.ITEM_INPUT);
        ItemStackIngredient solidIngredient = ItemStackIngredient.deserialize(itemInput);
        JsonElement fluidInput = JSONUtils.isJsonArray(json, JsonConstants.FLUID_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.FLUID_INPUT) :
                                 JSONUtils.getJsonObject(json, JsonConstants.FLUID_INPUT);
        FluidStackIngredient fluidIngredient = FluidStackIngredient.deserialize(fluidInput);
        JsonElement gasInput = JSONUtils.isJsonArray(json, JsonConstants.GAS_INPUT) ? JSONUtils.getJsonArray(json, JsonConstants.GAS_INPUT) :
                               JSONUtils.getJsonObject(json, JsonConstants.GAS_INPUT);
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(gasInput);
        FloatingLong energyRequired = FloatingLong.ZERO;
        if (json.has(JsonConstants.ENERGY_REQUIRED)) {
            energyRequired = SerializerHelper.getFloatingLong(json, JsonConstants.ENERGY_REQUIRED);
        }

        int duration;
        JsonElement ticks = json.get(JsonConstants.DURATION);
        if (!JSONUtils.isNumber(ticks)) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        duration = ticks.getAsJsonPrimitive().getAsInt();
        if (duration <= 0) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        ItemStack itemOutput = ItemStack.EMPTY;
        GasStack gasOutput = GasStack.EMPTY;
        if (json.has(JsonConstants.ITEM_OUTPUT)) {
            itemOutput = SerializerHelper.getItemStack(json, JsonConstants.ITEM_OUTPUT);
            if (itemOutput.isEmpty()) {
                throw new JsonSyntaxException("Reaction chamber item output must not be empty, if it is defined.");
            }
            if (json.has(JsonConstants.GAS_OUTPUT)) {
                //The gas is optional given we have an output item
                gasOutput = SerializerHelper.getGasStack(json, JsonConstants.GAS_OUTPUT);
                if (gasOutput.isEmpty()) {
                    throw new JsonSyntaxException("Reaction chamber gas output must not be empty, if it is defined.");
                }
            }
        } else {
            //If we don't have an output item, we are required to have an output gas
            gasOutput = SerializerHelper.getGasStack(json, JsonConstants.GAS_OUTPUT);
            if (gasOutput.isEmpty()) {
                throw new JsonSyntaxException("Reaction chamber gas output must not be empty, if there is no item output.");
            }
        }
        return this.factory.create(recipeId, solidIngredient, fluidIngredient, gasIngredient, energyRequired, duration, itemOutput, gasOutput);
    }

    @Override
    public RECIPE read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            ItemStackIngredient inputSolid = ItemStackIngredient.read(buffer);
            FluidStackIngredient inputFluid = FluidStackIngredient.read(buffer);
            GasStackIngredient inputGas = GasStackIngredient.read(buffer);
            FloatingLong energyRequired = FloatingLong.readFromBuffer(buffer);
            int duration = buffer.readVarInt();
            ItemStack outputItem = buffer.readItemStack();
            GasStack outputGas = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading pressurized reaction recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing pressurized reaction recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends PressurizedReactionRecipe> {

        RECIPE create(ResourceLocation id, ItemStackIngredient itemInput, FluidStackIngredient fluidInput, GasStackIngredient gasInput, FloatingLong energyRequired, int duration,
              ItemStack outputItem, GasStack outputGas);
    }
}