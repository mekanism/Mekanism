package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder> {

    private final FluidStackIngredient input;
    private final FluidStack output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStack output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Creates an Evaporating recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This evaporating recipe requires a non empty fluid output.");
        }
        return new FluidToFluidRecipeBuilder(input, output);
    }

    @Override
    protected FluidToFluidRecipe asRecipe() {
        return new BasicFluidToFluidRecipe(input, output);
    }
}