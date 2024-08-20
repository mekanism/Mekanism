package mekanism.client.recipe_viewer.jei;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mezz.jei.api.ingredients.IIngredientHelper;

public class MekanismJEIHelper implements IMekanismJEIHelper {

    public static final MekanismJEIHelper INSTANCE = new MekanismJEIHelper();

    private MekanismJEIHelper() {
    }

    @Override
    public IIngredientHelper<ChemicalStack> getChemicalStackHelper() {
        return MekanismJEI.CHEMICAL_STACK_HELPER;
    }
}