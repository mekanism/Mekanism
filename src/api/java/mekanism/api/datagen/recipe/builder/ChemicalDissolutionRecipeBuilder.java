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
    private final ChemicalStackIngredient chemicalInput;
    private final ChemicalStack output;
    private final boolean perTickUsage;

    protected ChemicalDissolutionRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ChemicalStack output, boolean perTickUsage) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.perTickUsage = perTickUsage;
    }

    /**
     * Creates a Chemical Dissolution recipe builder.
     *
     * @param itemInput     Item Input.
     * @param chemicalInput Chemical Input.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public static ChemicalDissolutionRecipeBuilder dissolution(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ChemicalStack output, boolean perTickUsage) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This dissolution chamber recipe requires a non empty chemical output.");
        }
        return new ChemicalDissolutionRecipeBuilder(itemInput, chemicalInput, output, perTickUsage);
    }

    @Override
    protected ChemicalDissolutionRecipe asRecipe() {
        return new BasicChemicalDissolutionRecipe(itemInput, chemicalInput, output, perTickUsage);
    }
}