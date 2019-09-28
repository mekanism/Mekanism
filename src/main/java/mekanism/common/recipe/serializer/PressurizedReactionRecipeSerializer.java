package mekanism.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PressurizedReactionRecipeSerializer<T extends PressurizedReactionIRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

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
        JsonElement gasInput = JSONUtils.isJsonArray(json, "gasInput") ? JSONUtils.getJsonArray(json, "gasInput") :
                               JSONUtils.getJsonObject(json, "gasInput");
        GasStackIngredient gasIngredient = GasStackIngredient.deserialize(gasInput);
        //TODO: Output is optional IFF output gas exists
        //TODO
        Gas outputGas = MekanismAPI.EMPTY_GAS;
        int outputGasAmount = 0;
        double energyRequired = 0;
        int duration = 1;
        ItemStack output = SerializerHelper.getItemStack(json, "output");
        return this.factory.create(recipeId, solidIngredient, fluidIngredient, gasIngredient, outputGas, outputGasAmount, energyRequired, duration, output);
    }

    @Override
    public T read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        ItemStackIngredient inputSolid = ItemStackIngredient.read(buffer);
        FluidStackIngredient inputFluid = FluidStackIngredient.read(buffer);
        GasStackIngredient gasInput = GasStackIngredient.read(buffer);
        Gas outputGas = buffer.readRegistryId();
        int outputGasAmount = buffer.readInt();
        double energyRequired = buffer.readDouble();
        int duration = buffer.readInt();
        ItemStack output = buffer.readItemStack();
        return this.factory.create(recipeId, inputSolid, inputFluid, gasInput, outputGas, outputGasAmount, energyRequired, duration, output);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull T recipe) {
        recipe.write(buffer);
    }

    public interface IFactory<T extends PressurizedReactionIRecipe> {

        T create(ResourceLocation id, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, Gas outputGas,
              int outputGasAmount, double energyRequired, int duration, ItemStack output);
    }
}