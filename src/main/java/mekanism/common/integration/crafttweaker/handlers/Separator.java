package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.separator")
@ModOnly("mtlib")
@ZenRegister
public class Separator
{
    public static final String NAME = "Mekanism Separator";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, double energy, IGasStack leftGasOutput, IGasStack rightGasOutput)
    {
        if (liquidInput == null || leftGasOutput == null || rightGasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        SeparatorRecipe recipe = new SeparatorRecipe(InputHelper.toFluid(liquidInput), energy, GasHelper.toGas(leftGasOutput), GasHelper.toGas(rightGasOutput));

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient liquidInput, @Optional IIngredient leftGasInput, @Optional IIngredient rightGasInput)
    {
        if (liquidInput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (leftGasInput == null)
            leftGasInput = IngredientAny.INSTANCE;
        if (rightGasInput == null)
            leftGasInput = IngredientAny.INSTANCE;

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get(), liquidInput, leftGasInput, rightGasInput));
    }

    private static class Remove extends RemoveMekanismRecipe<FluidInput, SeparatorRecipe>
    {
        private IIngredient liquidInput;
        private IIngredient leftGasInput;
        private IIngredient rightGasInput;

        public Remove(String name, Map<FluidInput, SeparatorRecipe> map, IIngredient liquidInput, IIngredient leftGasInput, IIngredient rightGasInput)
        {
            super(name, map);
            this.liquidInput = liquidInput;
            this.leftGasInput = leftGasInput;
            this.rightGasInput = rightGasInput;
        }

        @Override
        public void addRecipes()
        {
            Map<FluidInput, SeparatorRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<FluidInput, SeparatorRecipe> entry : RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get().entrySet())
            {
                ILiquidStack inputLiquid = InputHelper.toILiquidStack(entry.getKey().ingredient);
                IGasStack outputItemLeft = new CraftTweakerGasStack(entry.getValue().recipeOutput.leftGas);
                IGasStack outputItemRight = new CraftTweakerGasStack(entry.getValue().recipeOutput.rightGas);

                if (!StackHelper.matches(liquidInput, inputLiquid))
                    continue;
                if (!GasHelper.matches(leftGasInput, outputItemLeft))
                    continue;
                if (!GasHelper.matches(rightGasInput, outputItemRight))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logInfo(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME, liquidInput.toString(), leftGasInput.toString(), rightGasInput.toString()));
            }
        }
    }
}
