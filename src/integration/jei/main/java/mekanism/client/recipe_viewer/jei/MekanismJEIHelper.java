package mekanism.client.recipe_viewer.jei;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mezz.jei.api.ingredients.IIngredientHelper;

public class MekanismJEIHelper implements IMekanismJEIHelper {

    @Override
    public IIngredientHelper<ChemicalStack> getChemicalStackHelper() {
        if (MekanismJEI.chemicalStackHelper == null) {
            throw new IllegalStateException("Ingredient helper requested before ingredients have been initialized");
        }
        return MekanismJEI.chemicalStackHelper;
    }
}