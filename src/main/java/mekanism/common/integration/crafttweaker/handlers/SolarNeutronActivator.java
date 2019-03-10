package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.LogHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
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
@ModOnly("mtlib")
@ZenRegister
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

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get(), recipe));
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

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get(), gasInput, gasOutput));
    }

    private static class Remove extends RemoveMekanismRecipe
    {
        private IIngredient gasInput;
        private IIngredient gasOutput;

        public Remove(String name, Map<MachineInput, MachineRecipe> map, IIngredient gasInput, IIngredient gasOutput)
        {
            super(name, map);
            this.gasInput = gasInput;
            this.gasOutput = gasOutput;
        }

        @Override
        public void addRecipes()
        {
            Map<MachineInput, MachineRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<GasInput, SolarNeutronRecipe> entry : ((Map<GasInput, SolarNeutronRecipe>) RecipeHandler.Recipe.SOLAR_NEUTRON_ACTIVATOR.get()).entrySet())
            {
                IGasStack inputGas = new CraftTweakerGasStack(entry.getKey().ingredient);
                IGasStack outputGas = new CraftTweakerGasStack(entry.getValue().recipeOutput.output);

                if (!GasHelper.matches(gasInput, inputGas))
                    continue;
                if (!GasHelper.matches(gasOutput, outputGas))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logInfo(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, gasInput.toString(), gasOutput.toString()));
            }
        }
    }
}
