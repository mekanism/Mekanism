package mekanism.api.recipes;

import java.util.Objects;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/// Input: ItemStack
///
/// Output: ItemStack
///
/// @apiNote There are currently three types of ItemStack to ItemStack recipe types:
/// - Crushing: Can be processed in Crushers and Crushing Factories.
/// - Enriching: Can be processed in Enrichment Chambers and Enriching Factories.
/// - Smelting: Can be processed in Energized Smelters, Smelting Factories, and Robits.
public abstract class ItemStackToItemStackRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends ItemInputRecipe<ItemStackTemplate, BOOK_CATEGORY, BOOK_INFO> {

    protected final RecipeType<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    public ItemStackToItemStackRecipe(RecipeType<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        super(commonInfo, bookInfo);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        if (test(input.item())) {
            return getOutput(input.item()).create();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public final RecipeType<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return this.recipeType;
    }

    /// Helper interface for constructing [ItemStackToItemStackRecipes][ItemStackToItemStackRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param input      Input ingredient.
        /// @param output     Output item.
        RECIPE create(CommonInfo commonInfo, BOOK_INFO bookInfo, ItemStackIngredient input, ItemStackTemplate output);
    }
}
