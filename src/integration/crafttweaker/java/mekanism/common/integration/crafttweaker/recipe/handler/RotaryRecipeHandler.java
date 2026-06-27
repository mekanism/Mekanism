package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.RotaryRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(RotaryRecipe.class)
public class RotaryRecipeHandler extends MekanismRecipeHandler<RotaryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RotaryRecipe> manager, RegistryAccess registryAccess, RecipeHolder<RotaryRecipe> recipeHolder) {
        RotaryRecipe recipe = recipeHolder.value();
        //Note: We take advantage of the fact that if we have a recipe we have at least one direction and that we can skip parameters
        // as if they were optional as we will skip the later one as well and then end up with the proper method
        return buildCommandString(manager, recipeHolder,
              recipe.hasFluidToChemical() ? recipe.getFluidInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasChemicalToFluid() ? recipe.getChemicalInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasFluidToChemical() ? recipe.getChemicalOutputDefinition() : SKIP_OPTIONAL_PARAM,
              recipe.hasChemicalToFluid() ? recipe.getFluidOutputDefinition() : SKIP_OPTIONAL_PARAM
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe, U o) {
        //Only support if the other is a rotary recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof RotaryRecipe other) {
            return recipe.hasFluidToChemical() && other.hasFluidToChemical() && ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) ||
                   recipe.hasChemicalToFluid() && other.hasChemicalToFluid() && ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RotaryRecipe> manager, RegistryAccess registryAccess, RotaryRecipe recipe) {
        if (recipe.hasFluidToChemical()) {
            if (recipe.hasChemicalToFluid()) {
                return decompose(recipe.getFluidInput(), recipe.getChemicalInput(), recipe.getChemicalOutputDefinition(), recipe.getFluidOutputDefinition());
            }
            return decompose(recipe.getFluidInput(), recipe.getChemicalOutputDefinition());
        }//Else has chemical to fluid
        return decompose(recipe.getChemicalInput(), recipe.getFluidOutputDefinition());
    }

    @Override
    public Optional<RotaryRecipe> recompose(IRecipeManager<? super RotaryRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof RotaryRecipeManager manager) {
            Optional<ChemicalStackIngredient> chemicalInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHEMICAL.input());
            Optional<IFluidStack> fluidOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.output());
            if (chemicalInput.isPresent() != fluidOutput.isPresent()) {
                throw new IllegalArgumentException("Mismatched chemical input and fluid output. Only one is present.");
            }
            Optional<CTFluidIngredient> fluidInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.input());
            Optional<ICrTChemicalStack> chemicalOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHEMICAL.output());
            if (fluidInput.isPresent() != chemicalOutput.isPresent()) {
                throw new IllegalArgumentException("Mismatched fluid input and chemical output. Only one is present.");
            }
            if (chemicalInput.isPresent()) {
                //noinspection OptionalIsPresent - Capturing lambdas
                if (fluidInput.isPresent()) {
                    return Optional.of(manager.makeRecipe(
                          fluidInput.get(),
                          chemicalInput.get(),
                          chemicalOutput.get(),
                          fluidOutput.get()
                    ));
                }
                return Optional.of(manager.makeRecipe(
                      chemicalInput.get(),
                      fluidOutput.get()
                ));
            } else if (fluidInput.isPresent()) {
                return Optional.of(manager.makeRecipe(
                      fluidInput.get(),
                      chemicalOutput.get()
                ));
            }
        }
        return Optional.empty();
    }
}