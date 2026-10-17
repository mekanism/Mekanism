package mekanism.common.recipe.builder;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;

public class ExtendedCookingRecipeBuilder<RECIPE extends AbstractCookingRecipe> extends BaseRecipeBuilder<CookingBookCategory, CookingBookInfo, ExtendedCookingRecipeBuilder<RECIPE>> {

    private final AbstractCookingRecipe.Factory<RECIPE> factory;
    private final Ingredient ingredient;
    private final int cookingTime;
    private float experience;

    private ExtendedCookingRecipeBuilder(Holder<Item> result, int count, Ingredient ingredient, int cookingTime, CookingBookCategory bookCategory,
          AbstractCookingRecipe.Factory<RECIPE> factory) {
        this.ingredient = ingredient;
        this.cookingTime = cookingTime;
        this.factory = factory;
        super(CookingBookInfo::new, bookCategory, result, count);
    }

    public static ExtendedCookingRecipeBuilder<BlastingRecipe> blasting(CookingBookCategory bookCategory, Holder<Item> result, Ingredient ingredient, int cookingTime) {
        return blasting(bookCategory, result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder<BlastingRecipe> blasting(CookingBookCategory bookCategory, Holder<Item> result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder<>(result, count, ingredient, cookingTime, bookCategory, BlastingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder<CampfireCookingRecipe> campfire(Holder<Item> result, Ingredient ingredient, int cookingTime) {
        return campfire(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder<CampfireCookingRecipe> campfire(Holder<Item> result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder<>(result, count, ingredient, cookingTime, CookingBookCategory.FOOD, CampfireCookingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder<SmeltingRecipe> smelting(CookingBookCategory bookCategory, Holder<Item> result, Ingredient ingredient, int cookingTime) {
        return smelting(bookCategory, result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder<SmeltingRecipe> smelting(CookingBookCategory bookCategory, Holder<Item> result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder<>(result, count, ingredient, cookingTime, bookCategory, SmeltingRecipe::new);
    }

    public static ExtendedCookingRecipeBuilder<SmokingRecipe> smoking(Holder<Item> result, Ingredient ingredient, int cookingTime) {
        return smoking(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder<SmokingRecipe> smoking(Holder<Item> result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder<>(result, count, ingredient, cookingTime, CookingBookCategory.FOOD, SmokingRecipe::new);
    }

    public ExtendedCookingRecipeBuilder<RECIPE> experience(float experience) {
        if (experience < 0) {
            throw new IllegalArgumentException("Experience cannot be negative.");
        }
        this.experience = experience;
        return this;
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(
              commonInfo(),
              bookInfo(),
              this.ingredient,
              this.result,
              this.experience,
              this.cookingTime
        );
    }
}