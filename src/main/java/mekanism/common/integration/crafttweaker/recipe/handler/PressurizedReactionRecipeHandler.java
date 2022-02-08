package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(PressurizedReactionRecipe.class)
public class PressurizedReactionRecipeHandler extends MekanismRecipeHandler<PressurizedReactionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, PressurizedReactionRecipe recipe) {
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
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager manager, PressurizedReactionRecipe recipe, U o) {
        //Only support if the other is a reaction recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof PressurizedReactionRecipe other) {
            return ingredientConflicts(recipe.getInputSolid(), other.getInputSolid()) &&
                   ingredientConflicts(recipe.getInputFluid(), other.getInputFluid()) &&
                   ingredientConflicts(recipe.getInputGas(), other.getInputGas());
        }
        return false;
    }
}