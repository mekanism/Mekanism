package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ItemStackToChemicalRecipeHandler<RECIPE extends ItemStackToChemicalRecipe<?, ?>> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, RECIPE recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, RECIPE recipe, U other) {
        //Only support if the other is an itemstack to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return recipeIsInstance(other) && ingredientConflicts(recipe.getInput(), ((ItemStackToChemicalRecipe<?, ?>) other).getInput());
    }

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(Recipe<?> other);

    @IRecipeHandler.For(ItemStackToGasRecipe.class)
    public static class ItemStackToGasRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToGasRecipe> {

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToGasRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToInfuseTypeRecipe.class)
    public static class ItemStackToInfuseTypeRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToInfuseTypeRecipe> {

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToInfuseTypeRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToPigmentRecipe.class)
    public static class ItemStackToPigmentRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToPigmentRecipe> {

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToPigmentRecipe;
        }
    }
}