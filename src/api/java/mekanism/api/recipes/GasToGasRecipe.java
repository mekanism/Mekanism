package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.util.ResourceLocation;

/**
 * Input: Gas
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently two types of Gas to Gas recipe types:
 * <ul>
 *     <li>Activating: Can be processed in a Solar Neutron Activator.</li>
 *     <li>Centrifuging: Can be processed in an Isotopic Centrifuge.</li>
 * </ul>
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GasToGasRecipe extends ChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public GasToGasRecipe(ResourceLocation id, GasStackIngredient input, GasStack output) {
        super(id, input, output);
    }
}