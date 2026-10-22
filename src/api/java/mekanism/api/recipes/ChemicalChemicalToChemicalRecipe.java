package mekanism.api.recipes;

import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/// Base class for defining chemical+chemical to chemical recipes.
///
/// Input: Two chemicals. The order of them does not matter.
///
/// Output: ChemicalStack
///
/// @apiNote There are currently two types of Chemical+Chemical to Chemical recipe types:
/// - Chemical Infusing: Can be processed in a Chemical Infuser.
/// - Pigment Mixing: Can be processed in a Pigment Mixer.
public abstract class ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends OrderlessTwoInputRecipe<Chemical, ChemicalStack, ChemicalStackIngredient, BiChemicalRecipeInput, ChemicalStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    private static final PlacementInfo IS_PLACEABLE = PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.empty()));
    @Nullable
    private PlacementInfo placementInfo;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ChemicalChemicalToChemicalRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    @Override
    public boolean matches(BiChemicalRecipeInput input, Level level) {
        return test(input.left(), input.right());
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = getInputA().hasNoMatchingInstances() || getInputB().hasNoMatchingInstances() ? PlacementInfo.NOT_PLACEABLE : IS_PLACEABLE;
        }
        return this.placementInfo;
    }

    /// Helper interface for constructing [ChemicalChemicalToChemicalRecipes][ChemicalChemicalToChemicalRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>, RECIPE extends ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param leftInput  Left input ingredient.
        /// @param rightInput Right input ingredient.
        /// @param output     Output chemical.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output);
    }
}