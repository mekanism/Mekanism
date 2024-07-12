package mekanism.tools.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class PaxelShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private PaxelShapedRecipeBuilder(ItemLike result, int count) {
        super(result, count);
        category(RecipeCategory.TOOLS);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(ItemLike result) {
        return shapedRecipe(result, 1);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(ItemLike result, int count) {
        return new PaxelShapedRecipeBuilder(result, count);
    }

    @Override
    protected Recipe<?> wrapRecipe(ShapedRecipe recipe) {
        return new PaxelRecipe(recipe);
    }
}