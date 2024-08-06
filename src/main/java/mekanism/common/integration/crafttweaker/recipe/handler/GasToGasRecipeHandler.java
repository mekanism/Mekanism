package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.GasToGasRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(GasToGasRecipe.class)
public class GasToGasRecipeHandler extends MekanismRecipeHandler<GasToGasRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super GasToGasRecipe> manager, RegistryAccess registryAccess, RecipeHolder<GasToGasRecipe> recipeHolder) {
        GasToGasRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe, U o) {
        //Only support if the other is a gas to gas recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof GasToGasRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super GasToGasRecipe> manager, RegistryAccess registryAccess, GasToGasRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<GasToGasRecipe> recompose(IRecipeManager<? super GasToGasRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof GasToGasRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output())
            ));
        }
        return Optional.empty();
    }
}