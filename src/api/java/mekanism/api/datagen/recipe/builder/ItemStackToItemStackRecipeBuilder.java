package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicCrushingRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory.CrushingBookInfo;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory.EnrichingBookInfo;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackToItemStackRecipeBuilder<BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ItemStackToItemStackRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ItemStackToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ItemStackIngredient input;
    private final ItemStackTemplate output;

    protected ItemStackToItemStackRecipeBuilder(ItemStackIngredient input, ItemStackTemplate output, ItemStackToItemStackRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
          BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /// Creates a Crushing recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToItemStackRecipeBuilder<CrushingBookCategory, CrushingBookInfo, ?> crushing(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder<>(input, output, BasicCrushingRecipe::new, CrushingBookInfo::new, CrushingBookCategory.MISC);
    }

    /// Creates an Enriching recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToItemStackRecipeBuilder<EnrichingBookCategory, EnrichingBookInfo, ?> enriching(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder<>(input, output, BasicEnrichingRecipe::new, EnrichingBookInfo::new, EnrichingBookCategory.MISC);
    }

    /// Creates a Smelting recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToItemStackRecipeBuilder<CookingBookCategory, CookingBookInfo, ?> smelting(ItemStackIngredient input, ItemStackTemplate output) {
        return new ItemStackToItemStackRecipeBuilder<>(input, output, BasicSmeltingRecipe::new, CookingBookInfo::new, CookingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), input, output);
    }
}