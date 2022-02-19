package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import net.minecraft.item.crafting.IRecipe;

public abstract class ItemStackToChemicalRecipeHandler<RECIPE extends ItemStackToChemicalRecipe<?, ?>> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, RECIPE recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinitionNew());
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, RECIPE recipe, U other) {
        //Only support if the other is an itemstack to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return recipeIsInstance(other) && ingredientConflicts(recipe.getInput(), ((ItemStackToChemicalRecipe<?, ?>) other).getInput());
    }

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(IRecipe<?> other);

    @IRecipeHandler.For(ItemStackToGasRecipe.class)
    public static class ItemStackToGasRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToGasRecipe> {

        @Override
        protected boolean recipeIsInstance(IRecipe<?> other) {
            return other instanceof ItemStackToGasRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToInfuseTypeRecipe.class)
    public static class ItemStackToInfuseTypeRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToInfuseTypeRecipe> {

        @Override
        protected boolean recipeIsInstance(IRecipe<?> other) {
            return other instanceof ItemStackToInfuseTypeRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToPigmentRecipe.class)
    public static class ItemStackToPigmentRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToPigmentRecipe> {

        @Override
        protected boolean recipeIsInstance(IRecipe<?> other) {
            return other instanceof ItemStackToPigmentRecipe;
        }
    }
}