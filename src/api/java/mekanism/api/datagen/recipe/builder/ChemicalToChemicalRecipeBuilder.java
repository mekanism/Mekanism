package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicActivatingRecipe;
import mekanism.api.recipes.basic.BasicCentrifugingRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;

/**
 * @since 10.7.0 Previously was GasToGasRecipeBuilder
 */
@NothingNullByDefault
public class ChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<ChemicalToChemicalRecipeBuilder> {

    private final ChemicalToChemicalRecipeBuilder.Factory factory;
    private final ChemicalStackIngredient input;
    private final ChemicalStack output;

    protected ChemicalToChemicalRecipeBuilder(ChemicalStackIngredient input, ChemicalStack output, ChemicalToChemicalRecipeBuilder.Factory factory) {
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
    public static ChemicalToChemicalRecipeBuilder activating(ChemicalStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This solar neutron activator recipe requires a non empty chemical output.");
        }
        return new ChemicalToChemicalRecipeBuilder(input, output, BasicActivatingRecipe::new);
    }

    /**
     * Creates a Centrifuging recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalToChemicalRecipeBuilder centrifuging(ChemicalStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This Isotopic Centrifuge recipe requires a non empty chemical output.");
        }
        return new ChemicalToChemicalRecipeBuilder(input, output, BasicCentrifugingRecipe::new);
    }

    @Override
    protected ChemicalToChemicalRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        ChemicalToChemicalRecipe create(ChemicalStackIngredient input, ChemicalStack output);
    }
}