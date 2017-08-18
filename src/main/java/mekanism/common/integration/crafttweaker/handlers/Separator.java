package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
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

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get(), recipe));
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

        Map<MachineInput, MachineRecipe> recipes = new HashMap<>();

        for (Map.Entry<FluidInput, SeparatorRecipe> entry : ((Map<FluidInput, SeparatorRecipe>) RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get()).entrySet())
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

            recipes.put(entry.getKey(), entry.getValue());
        }

        if (!recipes.isEmpty())
        {
            CraftTweakerAPI.apply(new RemoveMekanismRecipe(NAME, RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.get(), recipes));
        }
        else
        {
            LogHelper.logWarning(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME, liquidInput.toString(), leftGasInput.toString(), rightGasInput.toString()));
        }
    }
}
