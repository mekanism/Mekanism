package mekanism.common.recipe.builder;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.Factory;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class ExtendedCookingRecipeBuilder extends BaseRecipeBuilder<ExtendedCookingRecipeBuilder> {

    private final AbstractCookingRecipe.Factory<?> factory;
    private final CookingBookCategory bookCategory;
    private final Ingredient ingredient;
    private final int cookingTime;
    private float experience;

    private ExtendedCookingRecipeBuilder(ItemLike result, int count, Ingredient ingredient, int cookingTime,
          CookingBookCategory bookCategory, Factory<?> factory) {
        super(result, count);
        this.ingredient = ingredient;
        this.cookingTime = cookingTime;
        this.bookCategory = bookCategory;
        this.factory = factory;
    }

    public static ExtendedCookingRecipeBuilder blasting(ItemLike result, Ingredient ingredient, int cookingTime) {
        return blasting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder blasting(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        CookingBookCategory bookCategory = result instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
        return new ExtendedCookingRecipeBuilder(result, count, ingredient, cookingTime, bookCategory, BlastingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder campfire(ItemLike result, Ingredient ingredient, int cookingTime) {
        return campfire(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder campfire(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(result, count, ingredient, cookingTime, CookingBookCategory.FOOD, CampfireCookingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder smelting(ItemLike result, Ingredient ingredient, int cookingTime) {
        return smelting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smelting(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        CookingBookCategory bookCategory;
        if (result.asItem().components().has(DataComponents.FOOD)) {
            bookCategory = CookingBookCategory.FOOD;
        } else {
            bookCategory = result instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
        }
        return new ExtendedCookingRecipeBuilder(result, count, ingredient, cookingTime, bookCategory, SmeltingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder smoking(ItemLike result, Ingredient ingredient, int cookingTime) {
        return smoking(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smoking(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(result, count, ingredient, cookingTime, CookingBookCategory.FOOD, SmokingRecipe::new);
    }

    public ExtendedCookingRecipeBuilder experience(float experience) {
        if (experience < 0) {
            throw new IllegalArgumentException("Experience cannot be negative.");
        }
        this.experience = experience;
        return this;
    }

    @Override
    protected Recipe<?> asRecipe() {
        return factory.create(
              Objects.requireNonNullElse(this.group, ""),
              bookCategory,
              this.ingredient,
              new ItemStack(this.result),
              this.experience,
              this.cookingTime
        );
    }
}