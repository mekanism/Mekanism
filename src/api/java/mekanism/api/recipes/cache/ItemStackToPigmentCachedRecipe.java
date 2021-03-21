package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToPigmentCachedRecipe extends ItemStackToChemicalCachedRecipe<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    public ItemStackToPigmentCachedRecipe(ItemStackToPigmentRecipe recipe, IInputHandler<@NonNull ItemStack> inputHandler,
          IOutputHandler<@NonNull PigmentStack> outputHandler) {
        super(recipe, inputHandler, outputHandler);
    }
}