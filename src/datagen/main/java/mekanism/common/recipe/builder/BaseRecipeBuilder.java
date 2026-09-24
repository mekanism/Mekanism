package mekanism.common.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    private final ItemStackTemplate result;
    protected RecipeCategory category = RecipeCategory.MISC;

    protected BaseRecipeBuilder(Holder<Item> result, int count) {
        this(new ItemStackTemplate(result, count));
    }

    protected BaseRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
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

    protected static Ingredient ingredient(HolderGetter<Item> lookup, ResourceKey<Item> id) {
        return ingredient(lookup.getOrThrow(id));
    }

    protected static Ingredient ingredient(Holder<Item> holder) {
        return Ingredient.of(HolderSet.direct(holder));
    }
}