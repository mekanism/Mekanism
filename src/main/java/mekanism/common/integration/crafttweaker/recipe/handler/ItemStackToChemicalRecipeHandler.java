package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ItemStackToChemicalRecipeHandler<
      RECIPE extends ItemStackToChemicalRecipe> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RecipeHolder<RECIPE> recipeHolder) {
        RECIPE recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
        //Only support if the other is an itemstack to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return recipeIsInstance(other) && ingredientConflicts(recipe.getInput(), ((ItemStackToChemicalRecipe) other).getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RECIPE recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackToChemicalRecipeManager) {
            ItemStackToChemicalRecipeManager<RECIPE> manager = (ItemStackToChemicalRecipeManager<RECIPE>) m;
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(getChemicalComponent().output())
            ));
        }
        return Optional.empty();
    }

    /**
     * @return Chemical component for recomposing recipes.
     */
    protected abstract ChemicalRecipeComponent getChemicalComponent();

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(Recipe<?> other);

    @IRecipeHandler.For(ItemStackToChemicalRecipe.class)
    public static class ItemStackToGasRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToChemicalRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToChemicalRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToPigmentRecipe.class)
    public static class ItemStackToPigmentRecipeHandler extends ItemStackToChemicalRecipeHandler<ItemStackToPigmentRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToPigmentRecipe;
        }
    }
}