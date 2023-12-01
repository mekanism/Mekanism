package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToItemStackRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ItemStackToItemStackRecipe.class)
public class ItemStackToItemStackRecipeHandler extends MekanismRecipeHandler<ItemStackToItemStackRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ItemStackToItemStackRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ItemStackToItemStackRecipe> recipeHolder) {
        ItemStackToItemStackRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackToItemStackRecipe> manager, ItemStackToItemStackRecipe recipe, U o) {
        //Only support if the other is an itemstack to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ItemStackToItemStackRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackToItemStackRecipe> manager, RegistryAccess registryAccess,
          ItemStackToItemStackRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ItemStackToItemStackRecipe> recompose(IRecipeManager<? super ItemStackToItemStackRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackToItemStackRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            ));
        }
        return Optional.empty();
    }
}