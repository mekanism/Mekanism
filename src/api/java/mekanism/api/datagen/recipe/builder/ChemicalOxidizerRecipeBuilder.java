package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalOxidizerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ChemicalOxidizerRecipeBuilder extends MekanismRecipeBuilder<ChemicalOxidizerRecipeBuilder> {

    private final ItemStackIngredient input;
    private final ChemicalStack<?> output;

    protected ChemicalOxidizerRecipeBuilder(ItemStackIngredient input, ChemicalStack<?> output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Creates a Chemical Oxidizer recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalOxidizerRecipeBuilder oxidizing(ItemStackIngredient input, ChemicalStack<?> output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This oxidizing recipe requires a non empty chemical output.");
        }
        return new ChemicalOxidizerRecipeBuilder(input, output);
    }

    @Override
    protected ChemicalOxidizerRecipe asRecipe() {
        return new BasicChemicalOxidizerRecipe(input, output);
    }
}