package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalConversionRecipe;
import mekanism.api.recipes.basic.BasicPigmentExtractingRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ItemStackToChemicalRecipeBuilder extends MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder> {

    private final ItemStackToChemicalRecipeBuilder.Factory factory;
    private final ItemStackIngredient input;
    private final ChemicalStack output;

    protected ItemStackToChemicalRecipeBuilder(ItemStackIngredient input, ChemicalStack output, ItemStackToChemicalRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Chemical Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder chemicalConversion(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This chemical conversion recipe requires a non empty chemical output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicChemicalConversionRecipe::new);
    }

    /**
     * Creates an Oxidizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder oxidizing(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty chemical output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicChemicalOxidizerRecipe::new);
    }

    /**
     * Creates a Pigment Extracting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder pigmentExtracting(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment extracting recipe requires a non empty chemical output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicPigmentExtractingRecipe::new);
    }

    @Override
    protected ItemStackToChemicalRecipe asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory {

        ItemStackToChemicalRecipe create(ItemStackIngredient input, ChemicalStack output);
    }
}