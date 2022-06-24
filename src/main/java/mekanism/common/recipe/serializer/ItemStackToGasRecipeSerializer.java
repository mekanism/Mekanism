package mekanism.common.recipe.serializer;

import com.google.gson.JsonObject;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToGasRecipeSerializer<RECIPE extends ItemStackToGasRecipe> extends ItemStackToChemicalRecipeSerializer<Gas, GasStack, RECIPE> {

    public ItemStackToGasRecipeSerializer(IFactory<Gas, GasStack, RECIPE> factory) {
        super(factory);
    }

    @Override
    protected GasStack fromJson(@NotNull JsonObject json, @NotNull String key) {
        return SerializerHelper.getGasStack(json, key);
    }

    @Override
    protected GasStack fromBuffer(@NotNull FriendlyByteBuf buffer) {
        return GasStack.readFromPacket(buffer);
    }
}