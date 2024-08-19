package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ItemStackToChemicalRecipe.class)
public class ItemStackToChemicalRecipeHandler extends MekanismRecipeHandler<ItemStackToChemicalRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ItemStackToChemicalRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ItemStackToChemicalRecipe> recipeHolder) {
        ItemStackToChemicalRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ItemStackToChemicalRecipe> manager, ItemStackToChemicalRecipe recipe, U o) {
        return o instanceof ItemStackToChemicalRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ItemStackToChemicalRecipe> manager, RegistryAccess registryAccess, ItemStackToChemicalRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ItemStackToChemicalRecipe> recompose(IRecipeManager<? super ItemStackToChemicalRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackToChemicalRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output())
            ));
        }
        return Optional.empty();
    }
}