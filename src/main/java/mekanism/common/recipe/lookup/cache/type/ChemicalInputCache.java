package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;

public class ChemicalInputCache<RECIPE extends MekanismRecipe<?>> extends BaseInputCache<Chemical, ChemicalStack, ChemicalStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient inputIngredient) {
        for (Holder<Chemical> chemicalHolder : inputIngredient.ingredient().getChemicalHolders()) {
            if (!chemicalHolder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                //Ignore empty stacks as some mods have ingredients that some stacks are empty
                addInputCache(chemicalHolder, recipe);
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