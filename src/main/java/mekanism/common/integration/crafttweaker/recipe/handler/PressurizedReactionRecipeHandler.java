package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.apache.commons.lang3.tuple.Pair;

@IRecipeHandler.For(PressurizedReactionRecipe.class)
public class PressurizedReactionRecipeHandler extends MekanismRecipeHandler<PressurizedReactionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager manager, PressurizedReactionRecipe recipe) {
        Pair<List<@NonNull ItemStack>, @NonNull GasStack> output = recipe.getOutputDefinition();
        //Note: We can handle skipping optional params like this because only one output should be empty at a time
        // if there is only a single output, which means we can safely skip the other
        return buildCommandString(manager, recipe, recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputGas(), recipe.getDuration(),
              output.getLeft().isEmpty() ? SKIP_OPTIONAL_PARAM : output.getLeft(),
              output.getRight().isEmpty() ? SKIP_OPTIONAL_PARAM : output.getRight(),
              recipe.getEnergyRequired().isZero() ? SKIP_OPTIONAL_PARAM : recipe.getEnergyRequired()
        );
    }

    @Override
    public <U extends IRecipe<?>> boolean doesConflict(IRecipeManager manager, PressurizedReactionRecipe recipe, U other) {
        //Only support if the other is a reaction recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (other instanceof PressurizedReactionRecipe) {
            PressurizedReactionRecipe otherRecipe = (PressurizedReactionRecipe) other;
            return ingredientConflicts(recipe.getInputSolid(), otherRecipe.getInputSolid()) &&
                   ingredientConflicts(recipe.getInputFluid(), otherRecipe.getInputFluid()) &&
                   ingredientConflicts(recipe.getInputGas(), otherRecipe.getInputGas());
        }
        return false;
    }
}