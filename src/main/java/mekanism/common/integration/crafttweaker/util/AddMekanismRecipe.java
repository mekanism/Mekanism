package mekanism.common.integration.crafttweaker.util;

import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.utils.BaseMapAddition;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;

import java.util.Map;

public class AddMekanismRecipe extends BaseMapAddition<MachineInput, MachineRecipe>
{
    private MachineRecipe recipe;

    public AddMekanismRecipe(String name, Map<MachineInput, MachineRecipe> map, MachineRecipe recipe)
    {
        super(name, map);
        this.recipe = recipe;
    }

    public void addRecipes()
    {
        this.recipes.put(recipe.getInput(), recipe);
    }

    @Override
    public void apply()
    {
        addRecipes();

        super.apply();
    }

    @Override
    protected String getRecipeInfo(Map.Entry<MachineInput, MachineRecipe> recipe)
    {
        MachineOutput output = recipe.getValue().recipeOutput;

        if (output instanceof ItemStackOutput)
        {
            return LogHelper.getStackDescription(((ItemStackOutput) output).output);
        }
        else if (output instanceof GasOutput)
        {
            return LogHelper.getStackDescription(((GasOutput) output).output);
        }
        else if (output instanceof FluidOutput)
        {
            return LogHelper.getStackDescription(((FluidOutput) output).output);
        }
        else if (output instanceof ChemicalPairOutput)
        {
            return "[" + LogHelper.getStackDescription(((ChemicalPairOutput) output).leftGas) + ", " + LogHelper.getStackDescription(((ChemicalPairOutput) output).rightGas) + "]";
        }
        else if (output instanceof ChanceOutput)
        {
            return LogHelper.getStackDescription(((ChanceOutput) output).primaryOutput);
        }
        else if (output instanceof PressurizedOutput)
        {
            return "[" + LogHelper.getStackDescription(((PressurizedOutput) output).getItemOutput()) + ", " + LogHelper.getStackDescription(((PressurizedOutput) output).getGasOutput()) + "]";
        }

        return null;
    }
}
