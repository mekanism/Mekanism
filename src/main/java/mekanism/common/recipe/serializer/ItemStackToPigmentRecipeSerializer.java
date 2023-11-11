package mekanism.common.recipe.serializer;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.common.recipe.impl.ChemicalOutputInternal;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ItemStackToPigmentRecipeSerializer<RECIPE extends ItemStackToPigmentRecipe & ChemicalOutputInternal<Pigment, PigmentStack>> extends ItemStackToChemicalRecipeSerializer<Pigment, PigmentStack, RECIPE> {

    public ItemStackToPigmentRecipeSerializer(IFactory<Pigment, PigmentStack, RECIPE> factory) {
        super(factory, ChemicalUtils.PIGMENT_STACK_CODEC);
    }

    @Override
    protected PigmentStack stackFromBuffer(@NotNull FriendlyByteBuf buffer) {
        return PigmentStack.readFromPacket(buffer);
    }
}