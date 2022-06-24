package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;

/**
 * Input: ItemStack
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently two types of ItemStack to Gas recipe types:
 * <ul>
 *     <li>Oxidizing: Can be processed in a Chemical Oxidizer.</li>
 *     <li>Gas Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to gases, for example in the Osmium Compressor and a variety of other machines.</li>
 * </ul>
 */
@ParametersAreNotNullByDefault
public abstract class ItemStackToGasRecipe extends ItemStackToChemicalRecipe<Gas, GasStack> {

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToGasRecipe(ResourceLocation id, ItemStackIngredient input, GasStack output) {
        super(id, input, output);
    }
}