package mekanism.client.jei.machine;

import mekanism.api.recipes.IMekanismRecipe;
import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class MekanismRecipeWrapper<RECIPE extends IMekanismRecipe> implements IRecipeWrapper {

    protected final RECIPE recipe;

    protected MekanismRecipeWrapper(RECIPE recipe) {
        this.recipe = recipe;
    }

    public RECIPE getRecipe() {
        return recipe;
    }
}