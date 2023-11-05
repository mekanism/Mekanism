package mekanism.api.recipes;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;

/**
 * Input: Two pigments. The order of them does not matter.
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Mixers can process this recipe type and the pigments can be put in any order into the mixer.
 */
@ParametersAreNotNullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    /**
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     *
     * @apiNote The order of the inputs does not matter.
     */
    public PigmentMixingRecipe(PigmentStackIngredient leftInput, PigmentStackIngredient rightInput, PigmentStack output) {
        super(leftInput, rightInput, output);
    }
}