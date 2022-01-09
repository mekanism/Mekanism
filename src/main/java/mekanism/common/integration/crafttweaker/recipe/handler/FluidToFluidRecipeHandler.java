package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.FluidToFluidRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(FluidToFluidRecipe.class)
public class FluidToFluidRecipeHandler extends MekanismRecipeHandler<FluidToFluidRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, FluidToFluidRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, FluidToFluidRecipe recipe, U o) {
        //Only support if the other is a fluid to fluid recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof FluidToFluidRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }
}