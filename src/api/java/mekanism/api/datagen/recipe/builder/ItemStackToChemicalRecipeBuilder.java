package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicPigmentExtractingRecipe;
import mekanism.api.recipes.book.ChemicalConversionBookCategory;
import mekanism.api.recipes.book.ChemicalConversionBookCategory.ChemicalConversionBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.OxidizingBookCategory;
import mekanism.api.recipes.book.OxidizingBookCategory.OxidizingBookInfo;
import mekanism.api.recipes.book.PigmentExtractingBookCategory;
import mekanism.api.recipes.book.PigmentExtractingBookCategory.PigmentExtractingBookInfo;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ItemStackToChemicalRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ItemStackToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ItemStackToChemicalRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ItemStackToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ItemStackIngredient input;
    private final ChemicalStackTemplate output;

    protected ItemStackToChemicalRecipeBuilder(ItemStackIngredient input, ChemicalStackTemplate output, ItemStackToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
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

    /// Creates a Chemical Conversion recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder<ChemicalConversionBookCategory, ChemicalConversionBookInfo, ?> chemicalConversion(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicChemicalConversionRecipe::new, ChemicalConversionBookInfo::new, ChemicalConversionBookCategory.MISC);
    }

    /// Creates an Oxidizing recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder<OxidizingBookCategory, OxidizingBookInfo, ?> oxidizing(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicChemicalOxidizerRecipe::new, OxidizingBookInfo::new, OxidizingBookCategory.MISC);
    }

    /// Creates a Pigment Extracting recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ItemStackToChemicalRecipeBuilder<PigmentExtractingBookCategory, PigmentExtractingBookInfo, ?> pigmentExtracting(ItemStackIngredient input, ChemicalStackTemplate output) {
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicPigmentExtractingRecipe::new, PigmentExtractingBookInfo::new, PigmentExtractingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), input, output);
    }
}