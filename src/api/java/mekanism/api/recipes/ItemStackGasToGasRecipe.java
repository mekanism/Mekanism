package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Inputs: ItemStack + GasStack Output: GasStack
 *
 * Chemical Dissolution Chamber
 *
 * @apiNote The gas input is a base value, and will still be multiplied by a per tick usage
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackGasToGasRecipe extends ItemStackChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    public ItemStackGasToGasRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, GasStack output) {
        super(id, itemInput, gasInput, output);
    }

    @Override
    public GasStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return output.copy();
    }
}