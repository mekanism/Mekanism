package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.PressurizedReactionRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(PressurizedReactionRecipe.class)
public class PressurizedReactionRecipeHandler extends MekanismRecipeHandler<PressurizedReactionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe) {
        ItemStack itemOutput;
        GasStack gasOutput;
        List<PressurizedReactionRecipeOutput> outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) {
            //Validate it isn't empty, which shouldn't be possible
            itemOutput = ItemStack.EMPTY;
            gasOutput = GasStack.EMPTY;
        } else {
            //Outputs sometimes are as lists, try wrapping them into a single element
            // eventually we may want to try listing them all somehow?
            PressurizedReactionRecipeOutput output = outputs.get(0);
            itemOutput = output.item();
            gasOutput = output.gas();
        }
        //Note: We can handle skipping optional params like this because only one output should be empty at a time
        // if there is only a single output, which means we can safely skip the other
        return buildCommandString(manager, recipe, recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputGas(), recipe.getDuration(),
              itemOutput.isEmpty() ? SKIP_OPTIONAL_PARAM : itemOutput, gasOutput.isEmpty() ? SKIP_OPTIONAL_PARAM : gasOutput,
              recipe.getEnergyRequired().isZero() ? SKIP_OPTIONAL_PARAM : recipe.getEnergyRequired()
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe, U o) {
        //Only support if the other is a reaction recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof PressurizedReactionRecipe other) {
            return ingredientConflicts(recipe.getInputSolid(), other.getInputSolid()) &&
                   ingredientConflicts(recipe.getInputFluid(), other.getInputFluid()) &&
                   ingredientConflicts(recipe.getInputGas(), other.getInputGas());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe) {
        return decompose(recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputGas(), recipe.getDuration(), recipe.getOutputDefinition(),
              recipe.getEnergyRequired());
    }

    @Override
    public Optional<PressurizedReactionRecipe> recompose(IRecipeManager<? super PressurizedReactionRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof PressurizedReactionRecipeManager manager) {
            Optional<IItemStack> optionalOutputItem = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ITEM.output());
            ItemStack outputItem;
            GasStack outputGas;
            if (optionalOutputItem.isPresent()) {
                outputItem = optionalOutputItem.get().getImmutableInternal();
                outputGas = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.GAS.output())
                      .map(ICrTChemicalStack::getImmutableInternal)
                      .orElse(GasStack.EMPTY);
            } else {
                outputItem = ItemStack.EMPTY;
                outputGas = recipe.getOrThrowSingle(CrTRecipeComponents.GAS.output()).getImmutableInternal();
            }
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
                  recipe.getOrThrowSingle(BuiltinRecipeComponents.Processing.TIME),
                  outputItem,
                  outputGas,
                  CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ENERGY).orElse(FloatingLong.ZERO)
            ));
        }
        return Optional.empty();
    }
}