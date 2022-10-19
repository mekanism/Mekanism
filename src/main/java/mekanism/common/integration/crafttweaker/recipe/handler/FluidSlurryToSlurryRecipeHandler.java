package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidSlurryToSlurryRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(FluidSlurryToSlurryRecipe.class)
public class FluidSlurryToSlurryRecipeHandler extends MekanismRecipeHandler<FluidSlurryToSlurryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe, U o) {
        //Only support if the other is a fluid slurry to slurry recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof FluidSlurryToSlurryRecipe other) {
            return ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super FluidSlurryToSlurryRecipe> manager, FluidSlurryToSlurryRecipe recipe) {
        return decompose(recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<FluidSlurryToSlurryRecipe> recompose(IRecipeManager<? super FluidSlurryToSlurryRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof FluidSlurryToSlurryRecipeManager manager) {
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.SLURRY.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.SLURRY.output())
            ));
        }
        return Optional.empty();
    }
}