package mekanism.api.recipes;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import net.minecraft.util.ResourceLocation;

/**
 * Input: Two gases. The order of them does not matter.
 * <br>
 * Output: GasStack
 *
 * @apiNote Chemical Infusers can process this recipe type and the gases can be put in any order into the infuser.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    /**
     * @param id         Recipe name.
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public ChemicalInfuserRecipe(ResourceLocation id, GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        super(id, leftInput, rightInput, output);
    }
}