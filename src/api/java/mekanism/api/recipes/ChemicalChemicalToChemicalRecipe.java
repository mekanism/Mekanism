package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Base class for defining chemical+chemical to chemical recipes.
 * <br>
 * Input: Two chemicals. The order of them does not matter.
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote There are currently two types of Chemical+Chemical to Chemical recipe types:
 * <ul>
 *     <li>Chemical Infusing: Can be processed in a Chemical Infuser.</li>
 *     <li>Pigment Mixing: Can be processed in a Pigment Mixer.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ChemicalChemicalToChemicalRecipe extends OrderlessTwoInputRecipe<Chemical, ChemicalStack, ChemicalStackIngredient, BiChemicalRecipeInput, ChemicalStack> {

    @Override
    public boolean matches(BiChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.left(), input.right());
    }
}