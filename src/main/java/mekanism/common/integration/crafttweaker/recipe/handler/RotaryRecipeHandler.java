package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.RotaryRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(RotaryRecipe.class)
public class RotaryRecipeHandler extends MekanismRecipeHandler<RotaryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, RotaryRecipe recipe) {
        //Note: We take advantage of the fact that if we have a recipe we have at least one direction and that we can skip parameters
        // as if they were optional as we will skip the later one as well and then end up with the proper method
        return buildCommandString(manager, recipe,
              recipe.hasFluidToGas() ? recipe.getFluidInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasGasToFluid() ? recipe.getGasInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasFluidToGas() ? recipe.getGasOutputDefinition() : SKIP_OPTIONAL_PARAM,
              recipe.hasGasToFluid() ? recipe.getFluidOutputDefinition() : SKIP_OPTIONAL_PARAM
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, RotaryRecipe recipe, U o) {
        //Only support if the other is a rotary recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof RotaryRecipe other) {
            return recipe.hasFluidToGas() && other.hasFluidToGas() && ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) ||
                   recipe.hasGasToFluid() && other.hasGasToFluid() && ingredientConflicts(recipe.getGasInput(), other.getGasInput());
        }
        return false;
    }
}