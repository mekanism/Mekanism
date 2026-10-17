package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ChemicalInputRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.crafting.Recipe;

/// Base class for defining chemical to chemical recipes.
///
/// Input: Chemical
///
/// Output: ChemicalStack
///
/// @apiNote There are currently two types of Chemical to Chemical recipe types:
/// - Activating: Can be processed in a Solar Neutron Activator.
/// - Centrifuging: Can be processed in an Isotopic Centrifuge.
public abstract class ChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ChemicalInputRecipe<ChemicalStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ChemicalToChemicalRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Helper interface for constructing [ChemicalToChemicalRecipes][ChemicalToChemicalRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param input      Input ingredient.
        /// @param output     Output chemical.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, ChemicalStackIngredient input, ChemicalStackTemplate output);
    }
}