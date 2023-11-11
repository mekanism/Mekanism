package mekanism.common.recipe.serializer;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.common.recipe.impl.ChemicalOutputInternal;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToInfuseTypeRecipeSerializer<RECIPE extends ItemStackToInfuseTypeRecipe & ChemicalOutputInternal<InfuseType, InfusionStack>> extends ItemStackToChemicalRecipeSerializer<InfuseType, InfusionStack, RECIPE> {

    public ItemStackToInfuseTypeRecipeSerializer(IFactory<InfuseType, InfusionStack, RECIPE> factory) {
        super(factory, ChemicalUtils.INFUSION_STACK_CODEC);
    }

    @Override
    protected InfusionStack stackFromBuffer(@NotNull FriendlyByteBuf buffer) {
        return InfusionStack.readFromPacket(buffer);
    }
}