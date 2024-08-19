package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.FluidChemicalToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(FluidChemicalToChemicalRecipe.class)
public class FluidSlurryToSlurryRecipeHandler extends MekanismRecipeHandler<FluidChemicalToChemicalRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super FluidChemicalToChemicalRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<FluidChemicalToChemicalRecipe> recipeHolder) {
        FluidChemicalToChemicalRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super FluidChemicalToChemicalRecipe> manager, FluidChemicalToChemicalRecipe recipe, U o) {
        if (o instanceof FluidChemicalToChemicalRecipe other) {
            return ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) &&
                   ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super FluidChemicalToChemicalRecipe> manager, RegistryAccess registryAccess, FluidChemicalToChemicalRecipe recipe) {
        return decompose(recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<FluidChemicalToChemicalRecipe> recompose(IRecipeManager<? super FluidChemicalToChemicalRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof FluidChemicalToChemicalRecipeManager manager) {
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output())
            ));
        }
        return Optional.empty();
    }
}