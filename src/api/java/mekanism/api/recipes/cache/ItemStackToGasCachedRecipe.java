package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackToGasCachedRecipe extends ItemStackToChemicalCachedRecipe<Gas, GasStack, ItemStackToGasRecipe> {

    public ItemStackToGasCachedRecipe(ItemStackToGasRecipe recipe, IInputHandler<@NonNull ItemStack> inputHandler, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe, inputHandler, outputHandler);
    }
}