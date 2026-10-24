
package mekanism.common.tests.recipe;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import mekanism.common.tests.helpers.RecipeTestHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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
        RegistryAccess registryAccess = helper.getLevel().registryAccess();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof MekanismRecipeType<?, ?, ?, ?, ?> mekanismRecipeType) {
                MekanismRecipeBookTypes bookCategories = mekanismRecipeType.getBookCategories();
                for (MekanismBookCategory bookCategory : bookCategories.categories()) {
                    helper.assertFalse(isEmpty(bookCategory.getPrimaryIcon(registryAccess)), "Primary icon for category " + bookCategory + " does not exist.");
                    Optional<Object> secondaryIcon = bookCategory.getSecondaryIcon(registryAccess);
                    secondaryIcon.ifPresent(stack -> helper.assertFalse(isEmpty(stack), "Secondary icon for category " + bookCategory + " does not exist."));
                }
            }
        }
        helper.succeed();
    }

    private static boolean isEmpty(Object object) {
        return switch (object) {
            case ItemStack stack -> stack.isEmpty();
            case FluidResource type -> type.isEmpty();
            case ChemicalResource type -> type.isEmpty();
            default -> false;
        };
    }
}