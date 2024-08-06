package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;

@NothingNullByDefault
public class ChemicalDissolutionRecipeBuilder extends MekanismRecipeBuilder<ChemicalDissolutionRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient gasInput;
    private final ChemicalStack output;

    protected ChemicalDissolutionRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient gasInput, ChemicalStack output) {
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.output = output;
    }

    /**
     * Creates a Chemical Dissolution recipe builder.
     *
     * @param itemInput Item Input.
     * @param gasInput  Gas Input.
     * @param output    Output.
     */
    public static ChemicalDissolutionRecipeBuilder dissolution(ItemStackIngredient itemInput, ChemicalStackIngredient gasInput, ChemicalStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This dissolution chamber recipe requires a non empty chemical output.");
        }
        return new ChemicalDissolutionRecipeBuilder(itemInput, gasInput, output);
    }

    @Override
    protected ChemicalDissolutionRecipe asRecipe() {
        return new BasicChemicalDissolutionRecipe(itemInput, gasInput, output);
    }
}