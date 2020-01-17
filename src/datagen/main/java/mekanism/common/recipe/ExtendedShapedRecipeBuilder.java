package mekanism.common.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.providers.IItemProvider;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapedRecipeBuilder extends ShapedRecipeBuilder {

    private ExtendedShapedRecipeBuilder(IItemProvider result, int countIn) {
        super(result, countIn);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(IItemProvider resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(IItemProvider resultIn, int countIn) {
        return new ExtendedShapedRecipeBuilder(resultIn, countIn);
    }

    public ExtendedShapedRecipeBuilder pattern(RecipePattern pattern) {
        patternLine(pattern.row1);
        if (pattern.row2 != null) {
            patternLine(pattern.row2);
            if (pattern.row3 != null) {
                patternLine(pattern.row3);
            }
        }
        return this;
    }

    @Override
    public ExtendedShapedRecipeBuilder key(Character symbol, Tag<Item> tag) {
        super.key(symbol, tag);
        return this;
    }

    @Override
    public ExtendedShapedRecipeBuilder key(Character symbol, net.minecraft.util.IItemProvider item) {
        super.key(symbol, item);
        return this;
    }

    @Override
    public ExtendedShapedRecipeBuilder key(Character symbol, Ingredient ingredient) {
        super.key(symbol, ingredient);
        return this;
    }

    @Override
    public ExtendedShapedRecipeBuilder addCriterion(String name, ICriterionInstance criterion) {
        super.addCriterion(name, criterion);
        return this;
    }

    public ExtendedShapedRecipeBuilder addCriterion(RecipeCriterion criterion) {
        return addCriterion(criterion.name, criterion.criterion);
    }

    @Override
    public ExtendedShapedRecipeBuilder setGroup(String group) {
        super.setGroup(group);
        return this;
    }
}