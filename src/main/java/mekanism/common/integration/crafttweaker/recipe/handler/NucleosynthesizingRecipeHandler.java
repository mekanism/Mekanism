package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(NucleosynthesizingRecipe.class)
public class NucleosynthesizingRecipeHandler extends MekanismRecipeHandler<NucleosynthesizingRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, NucleosynthesizingRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, NucleosynthesizingRecipe recipe, U o) {
        //Only support if the other is a nucleosynthesizing recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof NucleosynthesizingRecipe other) {
            return ingredientConflicts(recipe.getItemInput(), other.getItemInput()) && ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }
}