package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;

@NothingNullByDefault
public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder> {

    private final FluidStackIngredient input;
    private final ChemicalStack leftGasOutput;
    private final ChemicalStack rightGasOutput;
    private long energyMultiplier = 1;

    protected ElectrolysisRecipeBuilder(FluidStackIngredient input, ChemicalStack leftGasOutput, ChemicalStack rightGasOutput) {
        this.input = input;
        this.leftGasOutput = leftGasOutput;
        this.rightGasOutput = rightGasOutput;
    }

    /**
     * Creates a Separating recipe builder.
     *
     * @param input          Input.
     * @param leftGasOutput  Left Output.
     * @param rightGasOutput Right Output.
     */
    public static ElectrolysisRecipeBuilder separating(FluidStackIngredient input, ChemicalStack leftGasOutput, ChemicalStack rightGasOutput) {
        if (leftGasOutput.isEmpty() || rightGasOutput.isEmpty()) {
            throw new IllegalArgumentException("This separating recipe requires non empty gas outputs.");
        }
        return new ElectrolysisRecipeBuilder(input, leftGasOutput, rightGasOutput);
    }

    /**
     * Sets the energy multiplier for this recipe.
     *
     * @param multiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. This value must be greater than or equal to one.
     */
    public ElectrolysisRecipeBuilder energyMultiplier(long multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be greater than or equal to one");
        }
        this.energyMultiplier = multiplier;
        return this;
    }

    @Override
    protected ElectrolysisRecipe asRecipe() {
        return new BasicElectrolysisRecipe(input, energyMultiplier, leftGasOutput, rightGasOutput);
    }
}