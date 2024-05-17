package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.basic.BasicFluidSlurryToSlurryRecipe;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;

@NothingNullByDefault
public class FluidSlurryToSlurryRecipeBuilder extends MekanismRecipeBuilder<FluidSlurryToSlurryRecipeBuilder> {

    private final SlurryStackIngredient slurryInput;
    private final FluidStackIngredient fluidInput;
    private final SlurryStack output;

    protected FluidSlurryToSlurryRecipeBuilder(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        this.fluidInput = fluidInput;
        this.slurryInput = slurryInput;
        this.output = output;
    }

    /**
     * Creates a Washing recipe builder.
     *
     * @param fluidInput  Fluid Input.
     * @param slurryInput Slurry Input.
     * @param output      Output.
     */
    public static FluidSlurryToSlurryRecipeBuilder washing(FluidStackIngredient fluidInput, SlurryStackIngredient slurryInput, SlurryStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This washing recipe requires a non empty slurry output.");
        }
        return new FluidSlurryToSlurryRecipeBuilder(fluidInput, slurryInput, output);
    }

    @Override
    protected FluidSlurryToSlurryRecipe asRecipe() {
        return new BasicFluidSlurryToSlurryRecipe(fluidInput, slurryInput, output);
    }
}