package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;

@NothingNullByDefault
public class FluidChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<FluidChemicalToChemicalRecipeBuilder> {

    private final ChemicalStackIngredient chemicalInput;
    private final FluidStackIngredient fluidInput;
    private final ChemicalStack output;

    protected FluidChemicalToChemicalRecipeBuilder(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack output) {
        this.fluidInput = fluidInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
    }

    /**
     * Creates a Washing recipe builder.
     *
     * @param fluidInput    Fluid Input.
     * @param chemicalInput Chemical Input.
     * @param output        Output.
     */
    public static FluidChemicalToChemicalRecipeBuilder washing(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This washing recipe requires a non empty chemical output.");
        }
        return new FluidChemicalToChemicalRecipeBuilder(fluidInput, chemicalInput, output);
    }

    @Override
    protected FluidChemicalToChemicalRecipe asRecipe() {
        return new BasicWashingRecipe(fluidInput, chemicalInput, output);
    }
}