package mekanism.common.recipe.serializer;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToGasRecipeSerializer<RECIPE extends ItemStackToGasRecipe> extends ItemStackToChemicalRecipeSerializer<Gas, GasStack, RECIPE> {

    public ItemStackToGasRecipeSerializer(IFactory<Gas, GasStack, RECIPE> factory) {
        super(factory, GasStack.CODEC);
    }

    @Override
    protected GasStack stackFromBuffer(@NotNull FriendlyByteBuf buffer) {
        return GasStack.readFromPacket(buffer);
    }
}