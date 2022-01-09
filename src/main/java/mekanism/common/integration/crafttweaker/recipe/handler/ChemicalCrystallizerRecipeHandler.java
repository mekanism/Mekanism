package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ChemicalCrystallizerRecipe.class)
public class ChemicalCrystallizerRecipeHandler extends MekanismRecipeHandler<ChemicalCrystallizerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ChemicalCrystallizerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, ChemicalCrystallizerRecipe recipe, U o) {
        //Only support if the other is a chemical crystallizer recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ChemicalCrystallizerRecipe other && chemicalIngredientConflicts(recipe.getInput(), other.getInput());
    }
}