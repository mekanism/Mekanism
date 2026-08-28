package mekanism.api.integration.jei;

import mekanism.api.chemical.ChemicalStack;
import mezz.jei.api.ingredients.IIngredientHelper;

/// Helper for interacting with Mekanism's internals related to JEI. Get an instance via [mekanism.api.IMekanismAccess#jeiHelper()] after ensuring that JEI is loaded.
public interface IMekanismJEIHelper {

    /// Gets the ingredient helper for [`chemicals`][mekanism.api.chemical.ChemicalStack].
    ///
    /// @throws IllegalStateException If called before JEI has initialized the helper via IModIngredientRegistration
    ///
    /// @since 10.7.0
    IIngredientHelper<ChemicalStack> getChemicalStackHelper();
}