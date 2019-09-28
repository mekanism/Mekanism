package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PressurizedReactionRecipeSerializer<T extends PressurizedReactionRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public PressurizedReactionRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement inputSolid = JSONUtils.isJsonArray(json, "inputSolid") ? JSONUtils.getJsonArray(json, "inputSolid") :
                                 JSONUtils.getJsonObject(json, "inputSolid");
        ItemStackIngredient solidIngredient = ItemStackIngredient.deserialize(inputSolid);
        JsonElement inputFluid = JSONUtils.isJsonArray(json, "inputFluid") ? JSONUtils.getJsonArray(json, "inputFluid") :
                                 JSONUtils.getJsonObject(json, "inputFluid");
        FluidStackIngredient fluidIngredient = FluidStackIngredient.deserialize(inputFluid);
        JsonElement inputGas = JSONUtils.isJsonArray(json, "inputGas") ? JSONUtils.getJsonArray(json, "inputGas") :
                               JSONUtils.getJsonObject(json, "inputGas");
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(inputGas);
        double energyRequired = 0;
        if (json.has("energyRequired")) {
            JsonElement energy = json.get("energyRequired");
            if (!JSONUtils.isNumber(energy)) {
                throw new JsonSyntaxException("Expected energyRequired to be a non negative number.");
            }
            energyRequired = energy.getAsJsonPrimitive().getAsDouble();
            if (energyRequired < 0) {
                throw new JsonSyntaxException("Expected energyRequired to be non negative.");
            }
        }

        int duration;
        JsonElement ticks = json.get("duration");
        if (!JSONUtils.isNumber(ticks)) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        duration = ticks.getAsJsonPrimitive().getAsInt();
        if (duration <= 0) {
            throw new JsonSyntaxException("Expected duration to be a number greater than zero.");
        }
        ItemStack outputItem = ItemStack.EMPTY;
        GasStack outputGas = GasStack.EMPTY;
        if (json.has("outputItem")) {
            outputItem = SerializerHelper.getItemStack(json, "outputItem");
            if (json.has("outputGas")) {
                //The gas is optional given we have an output item
                outputGas = SerializerHelper.getGasStack(json, "outputGas");
            }
        } else {
            //If we don't have an output item, we are required to have an output gas
            outputGas = SerializerHelper.getGasStack(json, "outputGas");
        }
        return this.factory.create(recipeId, solidIngredient, fluidIngredient, gasIngredient, energyRequired, duration, outputItem, outputGas);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient inputSolid = ItemStackIngredient.read(buffer);
        FluidStackIngredient inputFluid = FluidStackIngredient.read(buffer);
        GasStackIngredient inputGas = GasStackIngredient.read(buffer);
        double energyRequired = buffer.readDouble();
        int duration = buffer.readInt();
        ItemStack outputItem = buffer.readItemStack();
        GasStack outputGas = GasStack.readFromPacket(buffer);
        return this.factory.create(recipeId, inputSolid, inputFluid, inputGas, energyRequired, duration, outputItem, outputGas);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends PressurizedReactionRecipe> {

        T create(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, double energyRequired, int duration,
              ItemStack outputItem, GasStack outputGas);
    }
}