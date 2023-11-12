package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class ElectrolysisIRecipe extends BasicElectrolysisRecipe {

    public ElectrolysisIRecipe(FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(input, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Override
    public RecipeSerializer<BasicElectrolysisRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

}