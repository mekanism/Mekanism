package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.SerializerHelper;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ElectrolysisRecipeSerializer<T extends ElectrolysisRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public ElectrolysisRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        JsonElement input = JSONUtils.isJsonArray(json, "input") ? JSONUtils.getJsonArray(json, "input") :
                            JSONUtils.getJsonObject(json, "input");
        FluidStackIngredient inputIngredient = FluidStackIngredient.deserialize(input);
        GasStack leftGasOutput = SerializerHelper.getGasStack(json, "leftGasOutput");
        GasStack rightGasOutput = SerializerHelper.getGasStack(json, "rightGasOutput");
        double energyMultiplier = 1;
        if (json.has("energyMultiplier")) {
            JsonElement energy = json.get("energyMultiplier");
            if (!JSONUtils.isNumber(energy)) {
                throw new JsonSyntaxException("Expected energyMultiplier to be a number greater than or equal to one.");
            }
            energyMultiplier = energy.getAsJsonPrimitive().getAsDouble();
            if (energyMultiplier < 1) {
                throw new JsonSyntaxException("Expected energyMultiplier to be at least one.");
            }
        }
        return this.factory.create(recipeId, inputIngredient, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            FluidStackIngredient input = FluidStackIngredient.read(buffer);
            double energyMultiplier = buffer.readDouble();
            GasStack leftGasOutput = GasStack.readFromPacket(buffer);
            GasStack rightGasOutput = GasStack.readFromPacket(buffer);
            return this.factory.create(recipeId, input, energyMultiplier, leftGasOutput, rightGasOutput);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading electrolysis recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing electrolysis recipe to packet.", e);
            throw e;
        }
    }

    public interface IFactory<T extends ElectrolysisRecipe> {

        T create(ResourceLocation id, FluidStackIngredient input, double energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput);
    }
}