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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ChemicalDissolutionRecipe.class)
public class ChemicalDissolutionRecipeHandler extends MekanismRecipeHandler<ChemicalDissolutionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe, U o) {
        //Only support if the other is a dissolution recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ChemicalDissolutionRecipe other) {
            return ingredientConflicts(recipe.getItemInput(), other.getItemInput()) &&
                   ingredientConflicts(recipe.getGasInput(), other.getGasInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalDissolutionRecipe> manager, ChemicalDissolutionRecipe recipe) {
        return decompose(recipe.getItemInput(), recipe.getGasInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ChemicalDissolutionRecipe> recompose(IRecipeManager<? super ChemicalDissolutionRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalDissolutionRecipeManager manager) {
            Optional<? extends ICrTChemicalStack<?, ?, ?>> output = CrTRecipeComponents.CHEMICAL_COMPONENTS.stream()
                  .map(chemicalComponent -> CrTUtils.getSingleIfPresent(recipe, chemicalComponent.output()))
                  .filter(Optional::isPresent)
                  .findFirst()
                  .flatMap(singleIfPresent -> singleIfPresent);
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
                  output.orElseThrow(() -> new IllegalArgumentException("No specified output chemical."))
            ));
        }
        return Optional.empty();
    }
}