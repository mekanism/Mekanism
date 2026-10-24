package mekanism.common.recipe.builder;

import java.util.function.BiFunction;
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
import org.jetbrains.annotations.Contract;

public abstract class BaseRecipeBuilder<BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>, BUILDER extends BaseRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, BUILDER>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, BUILDER> {

    protected final ItemStackTemplate result;

    protected BaseRecipeBuilder(BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory, Holder<Item> result, int count) {
        this(bookInfoCreator, defaultCategory, new ItemStackTemplate(result, count));
    }

    protected BaseRecipeBuilder(BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory, ItemStackTemplate result) {
        this.result = result;
        super(bookInfoCreator, defaultCategory);
    }

    /// Sets the recipe book category
    ///
    /// @param category Recipe book category to set
    @Contract("_ -> this")
    public BUILDER category(RecipeCategory category) {
        return advancementCategory(category.getFolderName());
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