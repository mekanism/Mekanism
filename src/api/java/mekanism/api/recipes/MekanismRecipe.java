package mekanism.api.recipes;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.book.MekanismBookCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

/// Base class for helping wrap our recipes into IRecipes.
public abstract class MekanismRecipe<INPUT extends RecipeInput, BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>> implements Recipe<INPUT> {

    private final Recipe.CommonInfo commonInfo;
    private final BOOK_INFO bookInfo;
    private final float experience;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param experience Amount of experience to that each process of the recipe contributes to the total experience.
    ///
    /// @since 10.8.0
    protected MekanismRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo, float experience) {
        this.commonInfo = Objects.requireNonNull(commonInfo, "Common recipe info cannot be null");
        this.bookInfo = Objects.requireNonNull(bookInfo, "Recipe Book info cannot be null");
        Preconditions.checkArgument(experience >= 0, "Experience cannot be negative.");
        this.experience = experience;
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

    public abstract void logMissingTags();

    @Override
    public ItemStack assemble(INPUT input) {
        return ItemStack.EMPTY;
    }

    @Override
    public final boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override//Force implementation
    public abstract List<RecipeDisplay> display();

    @Override
    public final RecipeBookCategory recipeBookCategory() {
        return bookInfo.category().toRecipeBookCategory();
    }

    /// Like vanilla's [net.minecraft.world.item.crafting.AbstractCookingRecipe#experience()] method
    ///
    /// @return Amount of experience to that each process of the recipe contributes to the total experience.
    ///
    /// @since 10.8.0
    public float experience() {
        return this.experience;
    }
}