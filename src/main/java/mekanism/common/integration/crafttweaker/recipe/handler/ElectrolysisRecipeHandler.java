package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.CrTUtils.UnaryTypePair;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ElectrolysisRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ElectrolysisRecipe.class)
public class ElectrolysisRecipeHandler extends MekanismRecipeHandler<ElectrolysisRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition(),
              recipe.getEnergyMultiplier().equals(FloatingLong.ONE) ? SKIP_OPTIONAL_PARAM : recipe.getEnergyMultiplier());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe, U o) {
        //Only support if the other is an electrolysis recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ElectrolysisRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ElectrolysisRecipe> manager, ElectrolysisRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition(), recipe.getEnergyMultiplier());
    }

    @Override
    public Optional<ElectrolysisRecipe> recompose(IRecipeManager<? super ElectrolysisRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ElectrolysisRecipeManager manager) {
            UnaryTypePair<ICrTGasStack> output = CrTUtils.getPair(recipe, CrTRecipeComponents.GAS.output());
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
                  output.a(),
                  output.b(),
                  CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ENERGY).orElse(FloatingLong.ONE)
            ));
        }
        return Optional.empty();
    }
}