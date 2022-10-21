package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import net.minecraft.resources.ResourceLocation;

/**
 * Input: Two gases. The order of them does not matter.
 * <br>
 * Output: GasStack
 *
 * @apiNote Chemical Infusers can process this recipe type and the gases can be put in any order into the infuser.
 */
@ParametersAreNotNullByDefault
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