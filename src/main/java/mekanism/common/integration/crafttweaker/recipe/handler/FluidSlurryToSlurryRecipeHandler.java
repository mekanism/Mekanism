package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(FluidSlurryToSlurryRecipe.class)
public class FluidSlurryToSlurryRecipeHandler extends MekanismRecipeHandler<FluidSlurryToSlurryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, FluidSlurryToSlurryRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, FluidSlurryToSlurryRecipe recipe, U other) {
        //Only support if the other is a fluid slurry to slurry recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (other instanceof FluidSlurryToSlurryRecipe) {
            FluidSlurryToSlurryRecipe otherRecipe = (FluidSlurryToSlurryRecipe) other;
            return ingredientConflicts(recipe.getFluidInput(), otherRecipe.getFluidInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), otherRecipe.getChemicalInput());
        }
        return false;
    }
}