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
 * Input: ItemStack
 * <br>
 * Input: Gas (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ItemStack
 *
 * @apiNote There are currently three types of ItemStack Gas to ItemStack recipe types:
 * <ul>
 *     <li>Compressing: Can be processed in Osmium Compressors and Compressing Factories.</li>
 *     <li>Injecting: Can be processed in Chemical Injection Chambers and Injecting Factories.</li>
 *     <li>Purifying: Can be processed in Purification Chambers and Purifying Factories.</li>
 * </ul>
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackGasToItemStackRecipe extends ItemStackChemicalToItemStackRecipe<Gas, GasStack, GasStackIngredient> {

    /**
     * @param id        Recipe name.
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     */
    public ItemStackGasToItemStackRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(id, itemInput, gasInput, output);
    }
}