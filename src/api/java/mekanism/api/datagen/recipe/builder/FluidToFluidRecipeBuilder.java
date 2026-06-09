package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.basic.BasicFluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class FluidToFluidRecipeBuilder extends MekanismRecipeBuilder<FluidToFluidRecipeBuilder> {

    private final FluidStackIngredient input;
    private final FluidStackTemplate output;

    protected FluidToFluidRecipeBuilder(FluidStackIngredient input, FluidStackTemplate output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /**
     * Creates an Evaporating recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static FluidToFluidRecipeBuilder evaporating(FluidStackIngredient input, FluidStackTemplate output) {
        return new FluidToFluidRecipeBuilder(input, output);
    }

    @Override
    protected FluidToFluidRecipe asRecipe() {
        return new BasicFluidToFluidRecipe(input, output);
    }
}