package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RotaryRecipeSerializer<T extends RotaryRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final IFactory<T> factory;

    public RotaryRecipeSerializer(IFactory<T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        FluidStackIngredient fluidInputIngredient = null;
        GasStackIngredient gasInputIngredient = null;
        GasStack gasOutput = null;
        FluidStack fluidOutput = null;
        boolean hasFluidToGas = false;
        boolean hasGasToFluid = false;
        if (json.has("fluidInput") || json.has("gasOutput")) {
            JsonElement fluidInput = JSONUtils.isJsonArray(json, "fluidInput") ? JSONUtils.getJsonArray(json, "fluidInput") :
                                     JSONUtils.getJsonObject(json, "fluidInput");
            fluidInputIngredient = FluidStackIngredient.deserialize(fluidInput);
            gasOutput = SerializerHelper.getGasStack(json, "gasOutput");
            hasFluidToGas = true;
        }
        if (json.has("gasInput") || json.has("fluidOutput")) {
            JsonElement gasInput = JSONUtils.isJsonArray(json, "gasInput") ? JSONUtils.getJsonArray(json, "gasInput") :
                                   JSONUtils.getJsonObject(json, "gasInput");
            gasInputIngredient = GasStackIngredient.deserialize(gasInput);
            fluidOutput = SerializerHelper.getFluidStack(json, "fluidOutput");
            hasGasToFluid = true;
        }
        if (hasFluidToGas && hasGasToFluid) {
            return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
        } else if (hasFluidToGas) {
            return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
        } else if (hasGasToFluid) {
            return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
        }
        throw new JsonSyntaxException("Rotary recipes require at least a gas to fluid or fluid to gas conversion.");
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        try {
            FluidStackIngredient fluidInputIngredient = null;
            GasStackIngredient gasInputIngredient = null;
            GasStack gasOutput = null;
            FluidStack fluidOutput = null;
            boolean hasFluidToGas = buffer.readBoolean();
            if (hasFluidToGas) {
                fluidInputIngredient = FluidStackIngredient.read(buffer);
                gasOutput = GasStack.readFromPacket(buffer);
            }
            boolean hasGasToFluid = buffer.readBoolean();
            if (hasGasToFluid) {
                gasInputIngredient = GasStackIngredient.read(buffer);
                fluidOutput = FluidStack.readFromPacket(buffer);
            }
            if (hasFluidToGas && hasGasToFluid) {
                return this.factory.create(recipeId, fluidInputIngredient, gasInputIngredient, gasOutput, fluidOutput);
            } else if (hasFluidToGas) {
                return this.factory.create(recipeId, fluidInputIngredient, gasOutput);
            } else if (hasGasToFluid) {
                return this.factory.create(recipeId, gasInputIngredient, fluidOutput);
            }
            //Should never happen, but if we somehow get here log it
            Mekanism.logger.error("Error reading rotary recipe from packet. A recipe got sent with no conversion in either direction.");
            return null;
        } catch (Exception e) {
            Mekanism.logger.error("Error reading rotary recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        if (recipe.hasFluidToGas() || recipe.hasGasToFluid()) {
            try {
                recipe.write(buffer);
            } catch (Exception e) {
                Mekanism.logger.error("Error writing rotary recipe to packet.", e);
                throw e;
            }
        } else {
            Mekanism.logger.error("Error writing rotary recipe to packet. {} has no conversion in either direction, so was not sent.", recipe);
        }
    }

    public interface IFactory<T extends RotaryRecipe> {

        T create(ResourceLocation id, FluidStackIngredient fluidInput, GasStack gasOutput);

        T create(ResourceLocation id, GasStackIngredient gasInput, FluidStack fluidOutput);

        T create(ResourceLocation id, FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack gasOutput, FluidStack fluidOutput);
    }
}