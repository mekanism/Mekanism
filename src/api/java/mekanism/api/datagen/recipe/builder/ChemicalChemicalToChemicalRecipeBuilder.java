package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory.ChemicalInfusingBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.PigmentMixingBookCategory;
import mekanism.api.recipes.book.PigmentMixingBookCategory.PigmentMixingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ChemicalChemicalToChemicalRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends ChemicalChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, ChemicalChemicalToChemicalRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final ChemicalChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ChemicalStackIngredient leftInput;
    private final ChemicalStackIngredient rightInput;
    private final ChemicalStackTemplate output;

    protected ChemicalChemicalToChemicalRecipeBuilder(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output,
          ChemicalChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator,
          BOOK_CATEGORY defaultCategory) {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates a Chemical Infusing recipe builder.
    ///
    /// @param leftInput  Left input.
    /// @param rightInput Right input.
    /// @param output     Output.
    public static ChemicalChemicalToChemicalRecipeBuilder<ChemicalInfusingBookCategory, ChemicalInfusingBookInfo, ?> chemicalInfusing(ChemicalStackIngredient leftInput,
          ChemicalStackIngredient rightInput, ChemicalStackTemplate output) {
        return new ChemicalChemicalToChemicalRecipeBuilder<>(leftInput, rightInput, output, BasicChemicalInfuserRecipe::new, ChemicalInfusingBookInfo::new, ChemicalInfusingBookCategory.MISC);
    }

    /// Creates a Pigment Mixing recipe builder.
    ///
    /// @param leftInput  Left input.
    /// @param rightInput Right input.
    /// @param output     Output.
    public static ChemicalChemicalToChemicalRecipeBuilder<PigmentMixingBookCategory, PigmentMixingBookInfo, ?> pigmentMixing(ChemicalStackIngredient leftInput,
          ChemicalStackIngredient rightInput, ChemicalStackTemplate output) {
        return new ChemicalChemicalToChemicalRecipeBuilder<>(leftInput, rightInput, output, BasicPigmentMixingRecipe::new, PigmentMixingBookInfo::new, PigmentMixingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), leftInput, rightInput, output);
    }
}