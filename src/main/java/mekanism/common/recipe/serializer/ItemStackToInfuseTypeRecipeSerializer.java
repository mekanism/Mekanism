package mekanism.common.recipe.serializer;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToInfuseTypeRecipeSerializer<RECIPE extends ItemStackToInfuseTypeRecipe> extends ItemStackToChemicalRecipeSerializer<InfuseType, InfusionStack, RECIPE> {

    public ItemStackToInfuseTypeRecipeSerializer(IFactory<InfuseType, InfusionStack, RECIPE> factory) {
        super(factory, InfusionStack.CODEC);
    }

    @Override
    protected InfusionStack stackFromBuffer(@NotNull FriendlyByteBuf buffer) {
        return InfusionStack.readFromPacket(buffer);
    }
}