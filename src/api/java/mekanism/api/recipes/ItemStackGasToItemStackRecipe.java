package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Inputs: ItemStack + GasStack Output: ItemStack
 *
 * Ex-AdvancedMachineInput based; InjectionRecipe, OsmiumCompressorRecipe, PurificationRecipe
 *
 * @apiNote The gas input is a base value, and will still be multiplied by a per tick usage
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackGasToItemStackRecipe extends ItemStackChemicalToItemStackRecipe<Gas, GasStack, GasStackIngredient> {

    public ItemStackGasToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id, itemInput, gasInput, output);
    }
}