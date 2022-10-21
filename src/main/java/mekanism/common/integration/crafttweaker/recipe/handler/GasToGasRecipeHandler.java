package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.GasToGasRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(GasToGasRecipe.class)
public class GasToGasRecipeHandler extends MekanismRecipeHandler<GasToGasRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe, U o) {
        //Only support if the other is a gas to gas recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof GasToGasRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super GasToGasRecipe> manager, GasToGasRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<GasToGasRecipe> recompose(IRecipeManager<? super GasToGasRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof GasToGasRecipeManager manager) {
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.GAS.output())
            ));
        }
        return Optional.empty();
    }
}