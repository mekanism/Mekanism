package mekanism.client.jei.machine;

import mekanism.common.recipe.machines.MachineRecipe;
import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class MekanismRecipeWrapper<RECIPE extends MachineRecipe> implements IRecipeWrapper {

    protected final RECIPE recipe;

    protected MekanismRecipeWrapper(RECIPE recipe) {
        this.recipe = recipe;
    }

    public RECIPE getRecipe() {
        return recipe;
    }
}