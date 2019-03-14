package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.mtlib.helpers.LogHelper;
import java.util.Map;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;

public class RecipeInfoHelper {

    private RecipeInfoHelper() {
    }

    public static String getRecipeInfo(Map.Entry<? extends MachineInput, ? extends MachineRecipe> recipe) {
        MachineOutput output = recipe.getValue().recipeOutput;
        if (output instanceof ItemStackOutput) {
            return LogHelper.getStackDescription(((ItemStackOutput) output).output);
        } else if (output instanceof GasOutput) {
            return getGasName(((GasOutput) output).output);
        } else if (output instanceof FluidOutput) {
            return LogHelper.getStackDescription(((FluidOutput) output).output);
        } else if (output instanceof ChemicalPairOutput) {
            ChemicalPairOutput out = (ChemicalPairOutput) output;
            return "[" + getGasName(out.leftGas) + ", " + getGasName(out.rightGas) + "]";
        } else if (output instanceof ChanceOutput) {
            return LogHelper.getStackDescription(((ChanceOutput) output).primaryOutput);
        } else if (output instanceof PressurizedOutput) {
            PressurizedOutput out = (PressurizedOutput) output;
            return "[" + LogHelper.getStackDescription(out.getItemOutput()) + ", " + getGasName(out.getGasOutput())
                  + "]";
        }
        return null;
    }

    public static String getGasName(GasStack stack) {
        return stack.amount > 1 ? String.format("<gas:%s> * %s", stack.getGas().getName(), stack.amount)
              : getGasName(stack.getGas());
    }

    public static String getGasName(Gas gas) {
        return String.format("<gas:%s>", gas.getName());
    }
}