package mekanism.common.integration.crafttweaker.util;

import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.utils.BaseMapAddition;
import java.util.Map;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;

public class AddMekanismRecipe<INPUT extends MachineInput<INPUT>, RECIPE extends MachineRecipe<INPUT, ?, RECIPE>> extends
      BaseMapAddition<INPUT, RECIPE> {

    private RECIPE recipe;

    public AddMekanismRecipe(String name, Map<INPUT, RECIPE> map, RECIPE recipe) {
        super(name, map);
        this.recipe = recipe;
        addRecipes();
    }

    public void addRecipes() {
        this.recipes.put(recipe.getInput(), recipe);
    }

    @Override
    protected String getRecipeInfo(Map.Entry<INPUT, RECIPE> recipe) {
        MachineOutput output = recipe.getValue().recipeOutput;

        if (output instanceof ItemStackOutput) {
            return LogHelper.getStackDescription(((ItemStackOutput) output).output);
        } else if (output instanceof GasOutput) {
            return LogHelper.getStackDescription(((GasOutput) output).output);
        } else if (output instanceof FluidOutput) {
            return LogHelper.getStackDescription(((FluidOutput) output).output);
        } else if (output instanceof ChemicalPairOutput) {
            return "[" + LogHelper.getStackDescription(((ChemicalPairOutput) output).leftGas) + ", " + LogHelper
                  .getStackDescription(((ChemicalPairOutput) output).rightGas) + "]";
        } else if (output instanceof ChanceOutput) {
            return LogHelper.getStackDescription(((ChanceOutput) output).primaryOutput);
        } else if (output instanceof PressurizedOutput) {
            return "[" + LogHelper.getStackDescription(((PressurizedOutput) output).getItemOutput()) + ", " + LogHelper
                  .getStackDescription(((PressurizedOutput) output).getGasOutput()) + "]";
        }

        return null;
    }
}
