package mekanism.common.recipe.builder;

import java.util.Objects;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    /// DO NOT MODIFY THIS
    private static final RecipeCategory[] VANILLA_CATEGORIES = RecipeCategory.values();

    protected final ItemStackTemplate result;

    protected BaseRecipeBuilder(Holder<Item> result, int count) {
        this(new ItemStackTemplate(result, count));
    }

    protected BaseRecipeBuilder(ItemStackTemplate result) {
        this.result = result;
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

    private static CraftingBookCategory determineCraftingBookCategory(@Nullable String category) {
        if (category != null) {
            for (RecipeCategory vanillaCategory : VANILLA_CATEGORIES) {
                if (vanillaCategory.getFolderName().equals(category)) {
                    return RecipeBuilder.determineCraftingBookCategory(vanillaCategory);
                }
            }
        }
        return CraftingBookCategory.MISC;
    }

    static CraftingRecipe.CraftingBookInfo createCraftingBookInfo(@Nullable String category, @Nullable String group) {
        return new CraftingRecipe.CraftingBookInfo(determineCraftingBookCategory(category), Objects.requireNonNullElse(group, ""));
    }
}