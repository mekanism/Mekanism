package mekanism.api.datagen.recipe.builder;

import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.api.recipes.book.WashingBookCategory.WashingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class FluidChemicalToChemicalRecipeBuilder<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
      extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, FluidChemicalToChemicalRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, RECIPE>> {

    private final FluidChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory;
    private final ChemicalStackIngredient chemicalInput;
    private final FluidStackIngredient fluidInput;
    private final ChemicalStackTemplate output;

    protected FluidChemicalToChemicalRecipeBuilder(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output,
          FluidChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        this.fluidInput = fluidInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.factory = factory;
        super(bookInfoCreator, defaultCategory);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates a Washing recipe builder.
    ///
    /// @param fluidInput    Fluid Input.
    /// @param chemicalInput Chemical Input.
    /// @param output        Output.
    public static FluidChemicalToChemicalRecipeBuilder<WashingBookCategory, WashingBookInfo, ?> washing(FluidStackIngredient fluidInput,
          ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output) {
        return new FluidChemicalToChemicalRecipeBuilder<>(fluidInput, chemicalInput, output, BasicWashingRecipe::new, WashingBookInfo::new, WashingBookCategory.MISC);
    }

    @Override
    protected RECIPE asRecipe() {
        return factory.create(commonInfo(), bookInfo(), fluidInput, chemicalInput, output);
    }
}