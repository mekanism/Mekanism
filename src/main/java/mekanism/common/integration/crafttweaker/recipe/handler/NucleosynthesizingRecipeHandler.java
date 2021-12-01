package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(NucleosynthesizingRecipe.class)
public class NucleosynthesizingRecipeHandler extends MekanismRecipeHandler<NucleosynthesizingRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, NucleosynthesizingRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, NucleosynthesizingRecipe recipe, U other) {
        //Only support if the other is a nucleosynthesizing recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (other instanceof NucleosynthesizingRecipe) {
            NucleosynthesizingRecipe otherRecipe = (NucleosynthesizingRecipe) other;
            return ingredientConflicts(recipe.getItemInput(), otherRecipe.getItemInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), otherRecipe.getChemicalInput());
        }
        return false;
    }
}