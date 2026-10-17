package mekanism.tools.common.recipe;

import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class PaxelShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private PaxelShapedRecipeBuilder(Holder<Item> result, int count) {
        super(result, count);
        category(RecipeCategory.TOOLS);
    }

    public static PaxelShapedRecipeBuilder shaped(Holder<Item> result) {
        return shaped(result, 1);
    }

    public static PaxelShapedRecipeBuilder shaped(Holder<Item> result, int count) {
        return new PaxelShapedRecipeBuilder(result, count);
    }

    @Override
    protected Recipe<?> wrapRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        return new PaxelRecipe(commonInfo, bookInfo, pattern, result);
    }
}