package mekanism.api.recipes;

import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

/// Base class for defining item chemical to item recipes.
///
/// Input: ItemStack
///
/// Input: Chemical
///
/// Output: ItemStack
///
/// @apiNote There are currently six types of ItemStack Chemical to ItemStack recipe types:
/// - Compressing: Can be processed in Osmium Compressors and Compressing Factories.
/// - Injecting: Can be processed in Chemical Injection Chambers and Injecting Factories.
/// - Purifying: Can be processed in Purification Chambers and Purifying Factories.
/// - Infusing: Can be processed in Metallurgic Infusers and Infusing Factories.
/// - Painting: Can be processed in Painting Machines.
/// - Nucleosynthesizing: Can be processed in the Antiprotonic Nucleosynthesizer.
public abstract class ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ItemStackChemicalToObjectRecipe<ItemStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ItemStackChemicalToItemStackRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    @Override
    public ItemStack assemble(SingleItemChemicalRecipeInput input) {
        if (!isIncomplete() && test(input.item(), input.chemical())) {
            return getOutput(input.item(), input.chemical()).create();
        }
        return ItemStack.EMPTY;
    }

    /// Helper interface for constructing [ItemStackChemicalToItemStackRecipes][ItemStackChemicalToItemStackRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends ItemStackChemicalToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo    Common recipe info.
        /// @param bookInfo      Recipe book info.
        /// @param itemInput     Item input ingredient.
        /// @param chemicalInput Chemical input ingredient.
        /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
        /// @param output        Output item.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage, ItemStackTemplate output);
    }
}