package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalDissolutionRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ChemicalDissolutionRecipe.class)
public class ChemicalDissolutionRecipeHandler extends MekanismRecipeHandler<ChemicalDissolutionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalDissolutionRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ChemicalDissolutionRecipe> recipeHolder) {
        ChemicalDissolutionRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.perTickUsage());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe, U o) {
        //Only support if the other is a dissolution recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ChemicalDissolutionRecipe other) {
            return ingredientConflicts(recipe.getItemInput(), other.getItemInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalDissolutionRecipe> manager, RegistryAccess registryAccess,
          ChemicalDissolutionRecipe recipe) {
        return decompose(recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.perTickUsage());
    }

    @Override
    public Optional<ChemicalDissolutionRecipe> recompose(IRecipeManager<? super ChemicalDissolutionRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalDissolutionRecipeManager manager) {
            Optional<? extends ICrTChemicalStack> found = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHEMICAL.output());
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  found.orElseThrow(() -> new IllegalArgumentException("No specified output chemical.")),
                  recipe.getOrThrowSingle(CrTRecipeComponents.PER_TICK_USAGE)
            ));
        }
        return Optional.empty();
    }
}