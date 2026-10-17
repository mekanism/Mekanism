package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicEvaporatingRecipe;
import mekanism.api.recipes.book.EvaporatingBookCategory;
import mekanism.api.recipes.book.EvaporatingBookCategory.EvaporatingBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class FluidToFluidRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends FluidToFluidRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, FluidToFluidRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final FluidToFluidRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final FluidStackIngredient input;
    private final FluidStackTemplate output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStackTemplate output, FluidToFluidRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
          BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates an Evaporating recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static FluidToFluidRecipeBuilder<EvaporatingBookCategory, EvaporatingBookInfo, ?> evaporating(FluidStackIngredient input, FluidStackTemplate output) {
        return new FluidToFluidRecipeBuilder<>(input, output, BasicEvaporatingRecipe::new, EvaporatingBookInfo::new, EvaporatingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), input, output);
    }
}