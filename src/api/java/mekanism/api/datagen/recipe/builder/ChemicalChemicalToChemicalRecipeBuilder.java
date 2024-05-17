package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicChemicalInfuserRecipe;
import mekanism.api.recipes.basic.BasicPigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;

@NothingNullByDefault
public class ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> extends MekanismRecipeBuilder<ChemicalChemicalToChemicalRecipeBuilder<CHEMICAL, STACK, INGREDIENT>> {

    private final ChemicalChemicalToChemicalRecipeBuilder.Factory<CHEMICAL, STACK, INGREDIENT> factory;
    private final INGREDIENT leftInput;
    private final INGREDIENT rightInput;
    private final STACK output;

    protected ChemicalChemicalToChemicalRecipeBuilder(INGREDIENT leftInput, INGREDIENT rightInput, STACK output,
          ChemicalChemicalToChemicalRecipeBuilder.Factory<CHEMICAL, STACK, INGREDIENT> factory) {
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
    public static ChemicalChemicalToChemicalRecipeBuilder<Gas, GasStack, GasStackIngredient> chemicalInfusing(GasStackIngredient leftInput, GasStackIngredient rightInput,
          GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This chemical infusing recipe requires a non empty gas output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder<>(leftInput, rightInput, output, BasicChemicalInfuserRecipe::new);
    }

    /**
     * Creates a Pigment Mixing recipe builder.
     *
     * @param leftInput  Left input.
     * @param rightInput Right input.
     * @param output     Output.
     */
    public static ChemicalChemicalToChemicalRecipeBuilder<Pigment, PigmentStack, PigmentStackIngredient> pigmentMixing(PigmentStackIngredient leftInput,
          PigmentStackIngredient rightInput, PigmentStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment mixing recipe requires a non empty gas output.");
        }
        return new ChemicalChemicalToChemicalRecipeBuilder<>(leftInput, rightInput, output, BasicPigmentMixingRecipe::new);
    }

    @Override
    protected ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT> asRecipe() {
        return factory.create(leftInput, rightInput, output);
    }

    @FunctionalInterface
    public interface Factory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> {

        ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT> create(INGREDIENT leftInput, INGREDIENT rightInput, STACK output);
    }
}