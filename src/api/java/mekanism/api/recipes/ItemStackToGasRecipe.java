package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.util.ResourceLocation;

/**
 * Input: ItemStack
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently three types of ItemStack to Gas recipe types:
 * <ul>
 *     <li>Oxidizing: Can be processed in a Chemical Oxidizer.</li>
 *     <li>Gas Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to gases, for example in the Osmium Compressor and a variety of other machines.</li>
 *     <li>Nutritional Liquification: These cannot currently be created, but are processed in the Nutritional Liquifier.</li>
 * </ul>
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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