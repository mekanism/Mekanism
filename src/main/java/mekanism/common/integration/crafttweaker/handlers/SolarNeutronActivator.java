package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.LogHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.solarneutronactivator")
public class SolarNeutronActivator
{
    public static final String NAME = "Mekanism Solar Neutron Activator";

    @ZenMethod
    public static void addRecipe(IGasStack gasInput, IGasStack gasOutput)
    {
        if (gasInput == null || gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        SolarNeutronRecipe recipe = new SolarNeutronRecipe(GasHelper.toGas(gasInput), GasHelper.toGas(gasOutput));

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasInput, @Optional IIngredient gasOutput)
    {
        if (gasInput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (gasOutput == null)
            gasOutput = IngredientAny.INSTANCE;

        Map<MachineInput, MachineRecipe> recipes = new HashMap<>();

        for (Map.Entry<GasInput, SolarNeutronRecipe> entry : ((Map<GasInput, SolarNeutronRecipe>) RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get()).entrySet())
        {
            IGasStack inputGas = new CraftTweakerGasStack(entry.getKey().ingredient);
            IGasStack outputGas = new CraftTweakerGasStack(entry.getValue().recipeOutput.output);

            if (!GasHelper.matches(gasInput, inputGas))
                continue;
            if (!GasHelper.matches(gasOutput, outputGas))
                continue;

            recipes.put(entry.getKey(), entry.getValue());
        }

        if (!recipes.isEmpty())
        {
            CraftTweakerAPI.apply(new RemoveMekanismRecipe(NAME, RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get(), recipes));
        }
        else
        {
            LogHelper.logWarning(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, gasInput.toString(), gasOutput.toString()));
        }
    }
}
