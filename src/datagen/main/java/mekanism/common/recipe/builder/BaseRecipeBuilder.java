package mekanism.common.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    private final ItemStackTemplate result;
    protected RecipeCategory category = RecipeCategory.MISC;

    protected BaseRecipeBuilder(Holder<Item> result, int count) {
        this.result = new ItemStackTemplate(result, count);
    }

    @SuppressWarnings("unchecked")
    public BUILDER category(RecipeCategory category) {
        this.category = category;
        return (BUILDER) this;
    }

    protected ItemStackTemplate resultStack() {
        return result;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(result);
    }
}