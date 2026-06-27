package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToEnergyRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ItemStackToEnergyRecipe.class)
public class ItemStackToEnergyRecipeHandler extends MekanismRecipeHandler<ItemStackToEnergyRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ItemStackToEnergyRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ItemStackToEnergyRecipe> recipeHolder) {
        ItemStackToEnergyRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackToEnergyRecipe> manager, ItemStackToEnergyRecipe recipe, U o) {
        //Only support if the other is an itemstack to energy recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ItemStackToEnergyRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackToEnergyRecipe> manager, RegistryAccess registryAccess, ItemStackToEnergyRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ItemStackToEnergyRecipe> recompose(IRecipeManager<? super ItemStackToEnergyRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackToEnergyRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ENERGY)
            ));
        }
        return Optional.empty();
    }
}