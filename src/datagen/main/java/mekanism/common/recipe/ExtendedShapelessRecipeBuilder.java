package mekanism.common.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapelessRecipeBuilder extends ShapelessRecipeBuilder {

    private ExtendedShapelessRecipeBuilder(IItemProvider result, int count) {
        super(result, count);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(IItemProvider result) {
        return shapelessRecipe(result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(IItemProvider result, int count) {
        return new ExtendedShapelessRecipeBuilder(result, count);
    }

    @Override
    public ExtendedShapelessRecipeBuilder addIngredient(Tag<Item> tag) {
        super.addIngredient(tag);
        return this;
    }

    @Override
    public ExtendedShapelessRecipeBuilder addIngredient(IItemProvider item) {
        super.addIngredient(item);
        return this;
    }

    @Override
    public ExtendedShapelessRecipeBuilder addIngredient(IItemProvider item, int quantity) {
        super.addIngredient(item, quantity);
        return this;
    }

    @Override
    public ExtendedShapelessRecipeBuilder addIngredient(Ingredient ingredient) {
        super.addIngredient(ingredient);
        return this;
    }

    @Override
    public ExtendedShapelessRecipeBuilder addIngredient(Ingredient ingredient, int quantity) {
        super.addIngredient(ingredient, quantity);
        return this;
    }

    @Override
    public ExtendedShapelessRecipeBuilder addCriterion(String name, ICriterionInstance criterion) {
        super.addCriterion(name, criterion);
        return this;
    }

    public ExtendedShapelessRecipeBuilder addCriterion(RecipeCriterion criterion) {
        return addCriterion(criterion.name, criterion.criterion);
    }

    @Override
    public ExtendedShapelessRecipeBuilder setGroup(String group) {
        super.setGroup(group);
        return this;
    }
}