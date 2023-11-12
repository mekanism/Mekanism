package mekanism.common.recipe.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

@NothingNullByDefault
public class ElectrolysisIRecipe extends BasicElectrolysisRecipe {

    public ElectrolysisIRecipe(FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(input, energyMultiplier, leftGasOutput, rightGasOutput);
    }

    @Override
    public RecipeType<ElectrolysisRecipe> getType() {
        return MekanismRecipeType.SEPARATING.get();
    }

    @Override
    public RecipeSerializer<ElectrolysisIRecipe> getSerializer() {
        return MekanismRecipeSerializers.SEPARATING.get();
    }

    @Override
    public String getGroup() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getName();
    }

    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ELECTROLYTIC_SEPARATOR.getItemStack();
    }

    public GasStack getLeftGasOutput() {
        return leftGasOutput;
    }

    public GasStack getRightGasOutput() {
        return rightGasOutput;
    }
}