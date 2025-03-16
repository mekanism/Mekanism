package mekanism.tools.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

@NothingNullByDefault
public class PaxelShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private PaxelShapedRecipeBuilder(Holder<Item> result, int count) {
        super(result, count);
        category(RecipeCategory.TOOLS);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(Holder<Item> result) {
        return shapedRecipe(result, 1);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(Holder<Item> result, int count) {
        return new PaxelShapedRecipeBuilder(result, count);
    }

    @Override
    protected Recipe<?> wrapRecipe(ShapedRecipe recipe) {
        return new PaxelRecipe(recipe);
    }
}