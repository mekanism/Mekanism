package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import net.minecraft.item.crafting.IRecipe;

@IRecipeHandler.For(ChemicalCrystallizerRecipe.class)
public class ChemicalCrystallizerRecipeHandler extends MekanismRecipeHandler<ChemicalCrystallizerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, ChemicalCrystallizerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, ChemicalCrystallizerRecipe recipe, U other) {
        //Only support if the other is a chemical crystallizer recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return other instanceof ChemicalCrystallizerRecipe && chemicalIngredientConflicts(recipe.getInput(), ((ChemicalCrystallizerRecipe) other).getInput());
    }
}