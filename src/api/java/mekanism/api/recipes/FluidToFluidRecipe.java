package mekanism.api.recipes;

import mekanism.api.recipes.SingleInputRecipe.FluidInputRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/// Input: FluidStack
///
/// Output: FluidStack
///
/// @apiNote Thermal Evaporation Towers can process this recipe type.
public abstract class FluidToFluidRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends FluidInputRecipe<FluidStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected FluidToFluidRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Helper interface for constructing [FluidToFluidRecipes][FluidToFluidRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>, RECIPE extends FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param input      Input ingredient.
        /// @param output     Output fluid.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, FluidStackIngredient input, FluidStackTemplate output);
    }
}
