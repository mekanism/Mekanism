package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicGasConversionRecipe;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ItemStackToChemicalRecipeBuilder extends
      MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder> {

    private final ItemStackToChemicalRecipeBuilder.Factory factory;
    private final ItemStackIngredient input;
    private final ChemicalStack output;

    protected ItemStackToChemicalRecipeBuilder(ItemStackIngredient input, ChemicalStack output, ItemStackToChemicalRecipeBuilder.Factory factory) {
        this.input = input;
        this.output = output;
        this.factory = factory;
    }

    /**
     * Creates a Gas Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder gasConversion(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicGasConversionRecipe::new);
    }

    /**
     * Creates an Oxidizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder oxidizing(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicChemicalOxidizerRecipe::new);
    }

    /**
     * Creates an Infusion Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder infusionConversion(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicItemStackToInfuseTypeRecipe::new);
    }

    /**
     * Creates a Pigment Extracting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder pigmentExtracting(ItemStackIngredient input, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment extracting recipe requires a non empty pigment output.");
        }
        return new ItemStackToChemicalRecipeBuilder(input, output, BasicItemStackToPigmentRecipe::new);
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