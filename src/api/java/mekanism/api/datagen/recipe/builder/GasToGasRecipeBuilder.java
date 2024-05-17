package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ingredients.GasStackIngredient;

@NothingNullByDefault
public class GasToGasRecipeBuilder extends MekanismRecipeBuilder<GasToGasRecipeBuilder> {

    private final GasToGasRecipeBuilder.Factory factory;
    private final GasStackIngredient input;
    private final GasStack output;

    protected GasToGasRecipeBuilder(GasStackIngredient input, GasStack output, GasToGasRecipeBuilder.Factory factory) {
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
    public static GasToGasRecipeBuilder activating(GasStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This solar neutron activator recipe requires a non empty gas output.");
        }
        return new GasToGasRecipeBuilder(input, output, BasicActivatingRecipe::new);
    }

    /**
     * Creates a Centrifuging recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static GasToGasRecipeBuilder centrifuging(GasStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This Isotopic Centrifuge recipe requires a non empty gas output.");
        }
        return new GasToGasRecipeBuilder(input, output, BasicCentrifugingRecipe::new);
    }

    @Override
    protected GasToGasRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        GasToGasRecipe create(GasStackIngredient input, GasStack output);
    }
}