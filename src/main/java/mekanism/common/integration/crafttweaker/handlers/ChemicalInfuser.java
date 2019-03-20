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
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.chemical.infuser")
@ModOnly("mtlib")
@ZenRegister
public class ChemicalInfuser
{
    public static final String NAME = "Mekanism Chemical Infuser";

    @ZenMethod
    public static void addRecipe(IGasStack leftGasInput, IGasStack rightGasInput, IGasStack gasOutput)
    {
        if (leftGasInput == null || rightGasInput == null || gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        ChemicalInfuserRecipe recipe = new ChemicalInfuserRecipe(GasHelper.toGas(leftGasInput), GasHelper.toGas(rightGasInput), GasHelper.toGas(gasOutput));

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, RecipeHandler.Recipe.CHEMICAL_INFUSER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasOutput, @Optional IIngredient leftGasInput, @Optional IIngredient rightGasInput)
    {
        if (gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (leftGasInput == null)
            leftGasInput = IngredientAny.INSTANCE;
        if (rightGasInput == null)
            rightGasInput = IngredientAny.INSTANCE;

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.CHEMICAL_INFUSER.get(), gasOutput, leftGasInput, rightGasInput));
    }

    private static class Remove extends RemoveMekanismRecipe<ChemicalPairInput,ChemicalInfuserRecipe>
    {
        private IIngredient gasOutput;
        private IIngredient leftGasInput;
        private IIngredient rightGasInput;

        public Remove(String name, Map<ChemicalPairInput,ChemicalInfuserRecipe> map, IIngredient gasOutput, IIngredient leftGasInput, IIngredient rightGasInput)
        {
            super(name, map);

            this.gasOutput = gasOutput;
            this.leftGasInput = leftGasInput;
            this.rightGasInput = rightGasInput;
        }

        @Override
        public void addRecipes()
        {
            Map<ChemicalPairInput,ChemicalInfuserRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<ChemicalPairInput, ChemicalInfuserRecipe> entry : RecipeHandler.Recipe.CHEMICAL_INFUSER.get().entrySet())
            {
                IGasStack inputGasLeft = new CraftTweakerGasStack(entry.getKey().leftGas);
                IGasStack inputGasRight = new CraftTweakerGasStack(entry.getKey().rightGas);
                IGasStack outputGas = new CraftTweakerGasStack(entry.getValue().recipeOutput.output);

                if (!GasHelper.matches(gasOutput, outputGas))
                    continue;
                if (!GasHelper.matches(leftGasInput, inputGasLeft))
                    continue;
                if (!GasHelper.matches(rightGasInput, inputGasRight))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logInfo(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME, gasOutput.toString(), leftGasInput.toString(), rightGasInput.toString()));
            }
        }
    }
}
