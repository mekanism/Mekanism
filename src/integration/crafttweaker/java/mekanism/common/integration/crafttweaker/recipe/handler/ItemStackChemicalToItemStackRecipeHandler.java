package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ItemStackChemicalToItemStackRecipe.class)
public class ItemStackChemicalToItemStackRecipeHandler extends MekanismRecipeHandler<ItemStackChemicalToItemStackRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ItemStackChemicalToItemStackRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder) {
        ItemStackChemicalToItemStackRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.perTickUsage());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackChemicalToItemStackRecipe> manager, ItemStackChemicalToItemStackRecipe recipe, U o) {
        //Only support if the other is an itemstack chemical to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ItemStackChemicalToItemStackRecipe other) {
            //Check chemical ingredients first in case the type doesn't match
            return chemicalIngredientConflicts(recipe.getChemicalInput(), other.getChemicalInput()) &&
                   ingredientConflicts(recipe.getItemInput(), other.getItemInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackChemicalToItemStackRecipe> manager, RegistryAccess registryAccess,
          ItemStackChemicalToItemStackRecipe recipe) {
        return decompose(recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.perTickUsage());
    }

    @Override
    public Optional<ItemStackChemicalToItemStackRecipe> recompose(IRecipeManager<? super ItemStackChemicalToItemStackRecipe> m, RegistryAccess registryAccess,
          IDecomposedRecipe recipe) {
        if (m instanceof ItemStackChemicalToItemStackRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.PER_TICK_USAGE)
            ));
        }
        return Optional.empty();
    }
}