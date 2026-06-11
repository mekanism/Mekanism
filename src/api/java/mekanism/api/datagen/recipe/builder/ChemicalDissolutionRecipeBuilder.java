package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ChemicalDissolutionRecipeBuilder extends MekanismRecipeBuilder<ChemicalDissolutionRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ChemicalStackTemplate output;
    private final boolean perTickUsage;

    protected ChemicalDissolutionRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output, boolean perTickUsage) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
        this.perTickUsage = perTickUsage;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates a Chemical Dissolution recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input.
    /// @param output        Output.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    public static ChemicalDissolutionRecipeBuilder dissolution(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output, boolean perTickUsage) {
        return new ChemicalDissolutionRecipeBuilder(itemInput, chemicalInput, output, perTickUsage);
    }

    @Override
    protected ChemicalDissolutionRecipe asRecipe() {
        return new BasicChemicalDissolutionRecipe(itemInput, chemicalInput, output, perTickUsage);
    }
}