package mekanism.api.integration.jei;

import mekanism.api.chemical.ChemicalStack;
import mezz.jei.api.ingredients.IIngredientHelper;

/**
 * Helper for interacting with Mekanism's internals related to JEI. Get an instance via {@link mekanism.api.IMekanismAccess#jeiHelper()} after ensuring that JEI is
 * loaded.
 */
public interface IMekanismJEIHelper {

    /**
     * Gets the ingredient helper for {@link mekanism.api.chemical.ChemicalStack gases}.
     */
    IIngredientHelper<ChemicalStack> getChemicalStackHelper();

}