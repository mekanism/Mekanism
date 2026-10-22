package mekanism.api.recipes;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/// Base class for defining fluid chemical to chemical recipes.
///
/// Input: FluidStack
///
/// Input: Chemical
///
/// Output: ChemicalStack
///
/// @apiNote Chemical Washers can process this recipe type.
public abstract class FluidChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> extends TwoInputMekRecipe<
      Fluid, FluidStack, FluidStackIngredient,
      Chemical, ChemicalStack, ChemicalStackIngredient,
      SingleFluidChemicalRecipeInput, ChemicalStackTemplate,
      BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected FluidChemicalToChemicalRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    @Override
    public boolean matches(SingleFluidChemicalRecipeInput input, Level level) {
        return test(input.fluid(), input.chemical());
    }

    /// Gets the input fluid ingredient.
    public abstract FluidStackIngredient getFluidInput();

    @Override
    public final FluidStackIngredient getInputA() {
        return getFluidInput();
    }

    /// Gets the input chemical ingredient.
    public abstract ChemicalStackIngredient getChemicalInput();

    @Override
    public final ChemicalStackIngredient getInputB() {
        return getChemicalInput();
    }

    /// Helper interface for constructing [FluidChemicalToChemicalRecipes][FluidChemicalToChemicalRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo    Common recipe info.
        /// @param bookInfo      Recipe book info.
        /// @param fluidInput    Fluid input ingredient.
        /// @param chemicalInput Chemical input ingredient.
        /// @param output        Output chemical.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output);
    }
}