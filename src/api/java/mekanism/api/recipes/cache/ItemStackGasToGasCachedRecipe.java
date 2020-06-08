package mekanism.api.recipes.cache;

import java.util.function.LongSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemStackGasToGasCachedRecipe extends ItemStackChemicalToChemicalCachedRecipe<Gas, GasStack, GasStackIngredient, ItemStackGasToGasRecipe> {

    public ItemStackGasToGasCachedRecipe(ItemStackGasToGasRecipe recipe, IInputHandler<@NonNull ItemStack> itemInputHandler,
          ILongInputHandler<@NonNull GasStack> gasInputHandler, LongSupplier gasUsage, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe, itemInputHandler, gasInputHandler, gasUsage, outputHandler);
    }
}