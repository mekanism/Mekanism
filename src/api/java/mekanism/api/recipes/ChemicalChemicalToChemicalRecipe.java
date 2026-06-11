package mekanism.api.recipes;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import net.minecraft.world.level.Level;

/// Base class for defining chemical+chemical to chemical recipes.
///
/// Input: Two chemicals. The order of them does not matter.
///
/// Output: ChemicalStack
///
/// @apiNote There are currently two types of Chemical+Chemical to Chemical recipe types:
/// - Chemical Infusing: Can be processed in a Chemical Infuser.
/// - Pigment Mixing: Can be processed in a Pigment Mixer.
public abstract class ChemicalChemicalToChemicalRecipe extends OrderlessTwoInputRecipe<Chemical, ChemicalStack, ChemicalStackIngredient, BiChemicalRecipeInput, ChemicalStackTemplate> {

    @Override
    public boolean matches(BiChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.left(), input.right());
    }
}