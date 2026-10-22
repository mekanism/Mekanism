
package mekanism.common.tests.recipe;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import mekanism.common.tests.helpers.RecipeTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "recipe.book")
public class RecipeBookTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to see if all the recipe book categories are unique.")
    public static void testRecipeBookCategories(final RecipeTestHelper helper) {
        Set<RecipeBookCategory> categories = new HashSet<>();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof MekanismRecipeType<?, ?, ?, ?, ?> mekanismRecipeType) {
                MekanismRecipeBookTypes bookCategories = mekanismRecipeType.getBookCategories();
                for (MekanismBookCategory bookCategory : bookCategories.categories()) {
                    if (!categories.add(bookCategory.toRecipeBookCategory())) {
                        helper.fail("Duplicate recipe category: " + bookCategory.getSerializedName());
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to see if all the recipe book icons are valid and exist.")
    public static void testRecipeBookIcons(final RecipeTestHelper helper) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof MekanismRecipeType<?, ?, ?, ?, ?> mekanismRecipeType) {
                MekanismRecipeBookTypes bookCategories = mekanismRecipeType.getBookCategories();
                for (MekanismBookCategory bookCategory : bookCategories.categories()) {
                    helper.assertFalse(bookCategory.getPrimaryIcon().isEmpty(), "Primary item for category " + bookCategory + " does not exist.");
                    Optional<ItemStack> secondaryIcon = bookCategory.getSecondaryIcon();
                    secondaryIcon.ifPresent(stack -> helper.assertFalse(stack.isEmpty(), "Secondary item for category " + bookCategory + " does not exist."));
                }
            }
        }
        helper.succeed();
    }
}