package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicGasConversionRecipe;
import mekanism.api.recipes.basic.BasicItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.basic.BasicItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ItemStackToChemicalRecipeBuilder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      MekanismRecipeBuilder<ItemStackToChemicalRecipeBuilder<CHEMICAL, STACK>> {

    private final ItemStackToChemicalRecipeBuilder.Factory<CHEMICAL, STACK> factory;
    private final ItemStackIngredient input;
    private final STACK output;

    protected ItemStackToChemicalRecipeBuilder(ItemStackIngredient input, STACK output, ItemStackToChemicalRecipeBuilder.Factory<CHEMICAL, STACK> factory) {
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
    public static ItemStackToChemicalRecipeBuilder<Gas, GasStack> gasConversion(ItemStackIngredient input, GasStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This gas conversion recipe requires a non empty gas output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicGasConversionRecipe::new);
    }

    /**
     * Creates an Infusion Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<InfuseType, InfusionStack> infusionConversion(ItemStackIngredient input, InfusionStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicItemStackToInfuseTypeRecipe::new);
    }

    /**
     * Creates a Pigment Extracting recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToChemicalRecipeBuilder<Pigment, PigmentStack> pigmentExtracting(ItemStackIngredient input, PigmentStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This pigment extracting recipe requires a non empty pigment output.");
        }
        return new ItemStackToChemicalRecipeBuilder<>(input, output, BasicItemStackToPigmentRecipe::new);
    }

    @Override
    protected ItemStackToChemicalRecipe<CHEMICAL, STACK> asRecipe() {
        return factory.create(input, output);
    }

    @FunctionalInterface
    public interface Factory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        ItemStackToChemicalRecipe<CHEMICAL, STACK> create(ItemStackIngredient input, STACK output);
    }
}