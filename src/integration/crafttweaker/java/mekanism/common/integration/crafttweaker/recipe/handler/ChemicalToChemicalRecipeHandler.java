package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ChemicalToChemicalRecipe.class)
public class ChemicalToChemicalRecipeHandler extends MekanismRecipeHandler<ChemicalToChemicalRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalToChemicalRecipe> manager, RegistryAccess registryAccess, RecipeHolder<ChemicalToChemicalRecipe> recipeHolder) {
        ChemicalToChemicalRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalToChemicalRecipe> manager, ChemicalToChemicalRecipe recipe, U o) {
        return o instanceof ChemicalToChemicalRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalToChemicalRecipe> manager, RegistryAccess registryAccess, ChemicalToChemicalRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ChemicalToChemicalRecipe> recompose(IRecipeManager<? super ChemicalToChemicalRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalToChemicalRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output())
            ));
        }
        return Optional.empty();
    }
}