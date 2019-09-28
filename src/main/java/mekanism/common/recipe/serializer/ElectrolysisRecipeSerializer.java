package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
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
        double energyUsage = 0;
        if (json.has("energyUsage")) {
            JsonElement energy = json.get("energyUsage");
            if (!JSONUtils.isNumber(energy)) {
                throw new JsonSyntaxException("Expected energyUsage to be a non negative number.");
            }
            energyUsage = energy.getAsJsonPrimitive().getAsDouble();
            if (energyUsage < 0) {
                throw new JsonSyntaxException("Expected secondaryChance to be non negative.");
            }
        }
        return this.factory.create(recipeId, inputIngredient, energyUsage, leftGasOutput, rightGasOutput);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        FluidStackIngredient input = FluidStackIngredient.read(buffer);
        double energy = buffer.readDouble();
        GasStack leftGasOutput = GasStack.readFromPacket(buffer);
        GasStack rightGasOutput = GasStack.readFromPacket(buffer);
        return this.factory.create(recipeId, input, energy, leftGasOutput, rightGasOutput);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends ElectrolysisRecipe> {

        T create(ResourceLocation id, FluidStackIngredient input, double energyUsage, GasStack leftGasOutput, GasStack rightGasOutput);
    }
}