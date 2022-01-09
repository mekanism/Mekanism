package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(FluidSlurryToSlurryRecipe.class)
public class FluidSlurryToSlurryRecipeHandler extends MekanismRecipeHandler<FluidSlurryToSlurryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, FluidSlurryToSlurryRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, FluidSlurryToSlurryRecipe recipe, U o) {
        //Only support if the other is a fluid slurry to slurry recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof FluidSlurryToSlurryRecipe other) {
            return ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }
}