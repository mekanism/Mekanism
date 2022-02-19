package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.FluidToFluidRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(FluidToFluidRecipe.class)
public class FluidToFluidRecipeHandler extends MekanismRecipeHandler<FluidToFluidRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, FluidToFluidRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, FluidToFluidRecipe recipe, U other) {
        //Only support if the other is a fluid to fluid recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof FluidToFluidRecipe && ingredientConflicts(recipe.getInput(), ((FluidToFluidRecipe) other).getInput());
    }
}