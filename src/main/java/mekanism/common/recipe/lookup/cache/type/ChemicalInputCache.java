package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

public class ChemicalInputCache<RECIPE extends MekanismRecipe<?>>
      extends BaseInputCache<Chemical, ChemicalStack, ChemicalStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient inputIngredient) {
        for (Chemical chemical : inputIngredient.ingredient().getChemicals()) {
            if (!chemical.isEmptyType()) {
                //Ignore empty stacks as some mods have ingredients that some stacks are empty
                addInputCache(chemical, recipe);
            }
        }
        return false;
    }

    @Override
    protected Chemical createKey(ChemicalStack stack) {
        return stack.getChemical();
    }

    @Override
    public boolean isEmpty(ChemicalStack input) {
        return input.isEmpty();
    }
}