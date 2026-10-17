package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.book.ActivatingBookCategory;
import mekanism.api.recipes.book.ActivatingBookCategory.ActivatingBookInfo;
import mekanism.api.recipes.book.CentrifugingBookCategory;
import mekanism.api.recipes.book.CentrifugingBookCategory.CentrifugingBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

/// @since 10.7.0 Previously was GasToGasRecipeBuilder
public class ChemicalToChemicalRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ChemicalToChemicalRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ChemicalStackIngredient input;
    private final ChemicalStackTemplate output;

    protected ChemicalToChemicalRecipeBuilder(ChemicalStackIngredient input, ChemicalStackTemplate output, ChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
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

    /// Creates an Activating recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ChemicalToChemicalRecipeBuilder<ActivatingBookCategory, ActivatingBookInfo, ?> activating(ChemicalStackIngredient input, ChemicalStackTemplate output) {
        return new ChemicalToChemicalRecipeBuilder<>(input, output, BasicActivatingRecipe::new, ActivatingBookInfo::new, ActivatingBookCategory.MISC);
    }

    /// Creates a Centrifuging recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ChemicalToChemicalRecipeBuilder<CentrifugingBookCategory, CentrifugingBookInfo, ?> centrifuging(ChemicalStackIngredient input, ChemicalStackTemplate output) {
        return new ChemicalToChemicalRecipeBuilder<>(input, output, BasicCentrifugingRecipe::new, CentrifugingBookInfo::new, CentrifugingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), input, output);
    }
}