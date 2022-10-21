package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.recipe.manager.RotaryRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(RotaryRecipe.class)
public class RotaryRecipeHandler extends MekanismRecipeHandler<RotaryRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe) {
        //Note: We take advantage of the fact that if we have a recipe we have at least one direction and that we can skip parameters
        // as if they were optional as we will skip the later one as well and then end up with the proper method
        return buildCommandString(manager, recipe,
              recipe.hasFluidToGas() ? recipe.getFluidInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasGasToFluid() ? recipe.getGasInput() : SKIP_OPTIONAL_PARAM,
              recipe.hasFluidToGas() ? recipe.getGasOutputDefinition() : SKIP_OPTIONAL_PARAM,
              recipe.hasGasToFluid() ? recipe.getFluidOutputDefinition() : SKIP_OPTIONAL_PARAM
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe, U o) {
        //Only support if the other is a rotary recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof RotaryRecipe other) {
            return recipe.hasFluidToGas() && other.hasFluidToGas() && ingredientConflicts(recipe.getFluidInput(), other.getFluidInput()) ||
                   recipe.hasGasToFluid() && other.hasGasToFluid() && ingredientConflicts(recipe.getGasInput(), other.getGasInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RotaryRecipe> manager, RotaryRecipe recipe) {
        if (recipe.hasFluidToGas()) {
            if (recipe.hasGasToFluid()) {
                return decompose(recipe.getFluidInput(), recipe.getGasInput(), recipe.getGasOutputDefinition(), recipe.getFluidOutputDefinition());
            }
            return decompose(recipe.getFluidInput(), recipe.getGasOutputDefinition());
        }//Else has gas to fluid
        return decompose(recipe.getGasInput(), recipe.getFluidOutputDefinition());
    }

    @Override
    public Optional<RotaryRecipe> recompose(IRecipeManager<? super RotaryRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof RotaryRecipeManager manager) {
            Optional<GasStackIngredient> gasInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.input());
            Optional<IFluidStack> fluidOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.output());
            if (gasInput.isPresent() != fluidOutput.isPresent()) {
                throw new IllegalArgumentException("Mismatched gas input and fluid output. Only one is present.");
            }
            Optional<FluidStackIngredient> fluidInput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.FLUID.input());
            Optional<ICrTGasStack> gasOutput = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.output());
            if (fluidInput.isPresent() != gasOutput.isPresent()) {
                throw new IllegalArgumentException("Mismatched fluid input and gas output. Only one is present.");
            }
            if (gasInput.isPresent()) {
                return fluidInput.map(fluidIngredient -> manager.makeRecipe(name,
                      fluidIngredient,
                      gasInput.get(),
                      gasOutput.get(),
                      fluidOutput.get()
                )).or(() -> Optional.of(manager.makeRecipe(name,
                      gasInput.get(),
                      fluidOutput.get()
                )));
            } else if (fluidInput.isPresent()) {
                return Optional.of(manager.makeRecipe(name,
                      fluidInput.get(),
                      gasOutput.get()
                ));
            }
        }
        return Optional.empty();
    }
}