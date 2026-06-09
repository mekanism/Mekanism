package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicWashingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class FluidChemicalToChemicalRecipeBuilder extends MekanismRecipeBuilder<FluidChemicalToChemicalRecipeBuilder> {

    private final ChemicalStackIngredient chemicalInput;
    private final FluidStackIngredient fluidInput;
    private final ChemicalStackTemplate output;

    protected FluidChemicalToChemicalRecipeBuilder(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output) {
        this.fluidInput = fluidInput;
        this.chemicalInput = chemicalInput;
        this.output = output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /**
     * Creates a Washing recipe builder.
     *
     * @param fluidInput    Fluid Input.
     * @param chemicalInput Chemical Input.
     * @param output        Output.
     */
    public static FluidChemicalToChemicalRecipeBuilder washing(FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output) {
        return new FluidChemicalToChemicalRecipeBuilder(fluidInput, chemicalInput, output);
    }

    @Override
    protected FluidChemicalToChemicalRecipe asRecipe() {
        return new BasicWashingRecipe(fluidInput, chemicalInput, output);
    }
}