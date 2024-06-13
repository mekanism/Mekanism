package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

public class ChemicalInputCache<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe<?>>
      extends BaseInputCache<CHEMICAL, STACK, ChemicalStackIngredient<CHEMICAL, STACK, ?>, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient<CHEMICAL, STACK, ?> inputIngredient) {
        for (CHEMICAL chemical : inputIngredient.ingredient().getChemicals()) {
            if (!chemical.isEmptyType()) {
                //Ignore empty stacks as some mods have ingredients that some stacks are empty
                addInputCache(chemical, recipe);
            }
        }
        return false;
    }

    @Override
    protected CHEMICAL createKey(STACK stack) {
        return stack.getChemical();
    }

    @Override
    public boolean isEmpty(STACK input) {
        return input.isEmpty();
    }
}