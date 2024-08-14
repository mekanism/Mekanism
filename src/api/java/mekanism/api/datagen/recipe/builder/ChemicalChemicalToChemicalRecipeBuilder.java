package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

@NothingNullByDefault
public class ChemicalChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder> {

    private final ChemicalChemicalToChemicalRecipeBuilder.Factory factory;
    private final ChemicalStackIngredient leftInput;
    private final ChemicalStackIngredient rightInput;
    private final ChemicalStack output;

    protected ChemicalChemicalToChemicalRecipeBuilder(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStack output,
          ChemicalChemicalToChemicalRecipeBuilder.Factory factory) {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Chemical Infusing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder chemicalInfusing(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput,
          ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This chemical infusing recipe requires a non empty chemical output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder(leftInput, rightInput, output, BasicChemicalInfuserRecipe::new);
    }

    /**
     * Creates a Pigment Mixing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder pigmentMixing(ChemicalStackIngredient leftInput,
          ChemicalStackIngredient rightInput, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment mixing recipe requires a non empty chemical output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder(leftInput, rightInput, output, BasicPigmentMixingRecipe::new);
    }

    @Override
    protected ChemicalChemicalToChemicalRecipe asRecipe() {
        return factory.create(leftInput, rightInput, output);
    }

    @FunctionalInterface
    public interface Factory {

        ChemicalChemicalToChemicalRecipe create(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStack output);
    }
}