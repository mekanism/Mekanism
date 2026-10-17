package mekanism.api.recipes;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.crafting.Recipe;

/// Base class for defining ItemStack to chemical recipes.
///
/// Input: ItemStack
///
/// Output: ChemicalStack
///
/// @apiNote There are currently three types of ItemStack to Chemical recipe types:
/// - Oxidizing: Can be processed in a Chemical Oxidizer.
/// - Chemical Conversion: Can be processed by any slots in Mekanism machines that are able to convert items to chemicals, for example in the Osmium Compressor and a
/// variety of other machines.
/// - Pigment Extracting: Can be processed in a Pigment Extractor.
public abstract class ItemStackToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ItemInputRecipe<ChemicalStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ItemStackToChemicalRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Helper interface for constructing [ItemStackToChemicalRecipes][ItemStackToChemicalRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param input      Input ingredient.
        /// @param output     Output chemical.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, ItemStackIngredient input, ChemicalStackTemplate output);
    }
}