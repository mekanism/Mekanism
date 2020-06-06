package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.cache.chemical.ChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class GasToItemStackCachedRecipe extends ChemicalToItemStackCachedRecipe<Gas, GasStack, GasStackIngredient, GasToItemStackRecipe> {

    public GasToItemStackCachedRecipe(GasToItemStackRecipe recipe, IInputHandler<@NonNull GasStack> inputHandler, IOutputHandler<@NonNull ItemStack> outputHandler) {
        super(recipe, inputHandler, outputHandler);
    }
}