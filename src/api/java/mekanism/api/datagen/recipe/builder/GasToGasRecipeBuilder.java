package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

@NothingNullByDefault
public class GasToGasRecipeBuilder extends MekanismRecipeBuilder<GasToGasRecipeBuilder> {

    private final GasToGasRecipeBuilder.Factory factory;
    private final ChemicalStackIngredient input;
    private final ChemicalStack output;

    protected GasToGasRecipeBuilder(ChemicalStackIngredient input, ChemicalStack output, GasToGasRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates an Activating recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static GasToGasRecipeBuilder activating(ChemicalStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This solar neutron activator recipe requires a non empty chemical output.");
        }
        return new GasToGasRecipeBuilder(input, output, BasicActivatingRecipe::new);
    }

    /**
     * Creates a Centrifuging recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static GasToGasRecipeBuilder centrifuging(ChemicalStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This Isotopic Centrifuge recipe requires a non empty chemical output.");
        }
        return new GasToGasRecipeBuilder(input, output, BasicCentrifugingRecipe::new);
    }

    @Override
    protected GasToGasRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        GasToGasRecipe create(ChemicalStackIngredient input, ChemicalStack output);
    }
}