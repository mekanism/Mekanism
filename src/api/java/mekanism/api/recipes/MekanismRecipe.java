package mekanism.api.recipes;

import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.book.MekanismBookCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

/// Base class for helping wrap our recipes into IRecipes.
public abstract class MekanismRecipe<INPUT extends RecipeInput, BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> implements Recipe<INPUT> {

    protected final Recipe.CommonInfo commonInfo;
    protected final BOOK_INFO bookInfo;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected MekanismRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        this.commonInfo = Objects.requireNonNull(commonInfo, "Common recipe info cannot be null");
        this.bookInfo = Objects.requireNonNull(bookInfo, "Recipe Book info cannot be null");
    }

    /// {@return common recipe information about this recipe}
    ///
    /// @since 10.8.0
    public final Recipe.CommonInfo commonInfo() {
        return commonInfo;
    }

    /// {@return recipe book information about this recipe}
    ///
    /// @since 10.8.0
    public final BOOK_INFO bookInfo() {
        return bookInfo;
    }

    @Override
    public final String group() {
        return bookInfo.group();
    }

    @Override
    public boolean isSpecial() {
        //Note: If we make this non-dynamic, we can make it show in vanilla's crafting book and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    //TODO - 26.3: Do we want to keep some form of this? Vanilla moved this to being part of PlacementInfo#isImpossibleToPlace ??
    public abstract boolean isIncomplete();

    public abstract void logMissingTags();

    @Override
    public ItemStack assemble(INPUT input) {
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        //TODO: Can we implement this?
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public final boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override//Force implementation
    public abstract List<RecipeDisplay> display();

    @Override
    public final RecipeBookCategory recipeBookCategory() {
        return switch (bookInfo.category()) {
            case MekanismBookCategory category -> category.toRecipeBookCategory();
            //From SmeltingRecipe#recipeBookCategory
            case CookingBookCategory category -> switch (category) {
                case BLOCKS -> RecipeBookCategories.FURNACE_BLOCKS;
                case FOOD -> RecipeBookCategories.FURNACE_FOOD;
                case MISC -> RecipeBookCategories.FURNACE_MISC;
            };
            default -> RecipeBookCategories.CRAFTING_MISC;
        };
    }
}