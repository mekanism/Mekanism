package mekanism.common.recipe.builder;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.datagen.recipe.RecipeCriterion;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapelessRecipeBuilder extends ShapelessRecipeBuilder {

    protected ExtendedShapelessRecipeBuilder(IItemProvider result, int count) {
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

    /**
     * {@inheritDoc}
     *
     * @deprecated Deprecating this method to make it easier to see when it is accidentally called, as it is probably an accident and was an attempt to call {@link
     * #build(Consumer, ResourceLocation)}
     */
    @Override
    @Deprecated
    public void build(Consumer<IFinishedRecipe> consumer, String save) {
        super.build(consumer, save);
    }
}