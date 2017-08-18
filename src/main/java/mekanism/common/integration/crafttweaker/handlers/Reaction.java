package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.outputs.PressurizedOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;


@ZenClass("mods.mekanism.reaction")
@ModOnly("mtlib")
@ZenRegister
public class Reaction
{
    public static final String NAME = "Mekanism Reaction";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, ILiquidStack liquidInput, IGasStack gasInput, IItemStack itemOutput, IGasStack gasOutput, double energy, int duration)
    {
        if (itemInput == null || liquidInput == null || gasInput == null || itemOutput == null || gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        PressurizedInput input = new PressurizedInput(InputHelper.toStack(itemInput), InputHelper.toFluid(liquidInput), GasHelper.toGas(gasInput));
        PressurizedOutput output = new PressurizedOutput(InputHelper.toStack(itemOutput), GasHelper.toGas(gasOutput));

        PressurizedRecipe recipe = new PressurizedRecipe(input, output, energy, duration);

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.PRESSURIZED_REACTION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, IIngredient gasOutput, @Optional IIngredient itemInput, @Optional IIngredient liquidInput, @Optional IIngredient gasInput)
    {
        if (itemOutput == null || gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (itemInput == null)
            itemInput = IngredientAny.INSTANCE;
        if (liquidInput == null)
            liquidInput = IngredientAny.INSTANCE;
        if (gasInput == null)
            gasInput = IngredientAny.INSTANCE;

        Map<MachineInput, MachineRecipe> recipes = new HashMap<>();

        for (Map.Entry<PressurizedInput, PressurizedRecipe> entry : ((Map<PressurizedInput, PressurizedRecipe>) RecipeHandler.Recipe.PRESSURIZED_REACTION_CHAMBER.get()).entrySet())
        {
            IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().getSolid());
            ILiquidStack inputLiquid = InputHelper.toILiquidStack(entry.getKey().getFluid());
            IGasStack inputGas = new CraftTweakerGasStack(entry.getKey().getGas());
            IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().recipeOutput.getItemOutput());
            IGasStack outputGas = new CraftTweakerGasStack(entry.getValue().recipeOutput.getGasOutput());

            if (!StackHelper.matches(itemInput, inputItem))
                continue;
            if (!StackHelper.matches(liquidInput, inputLiquid))
                continue;
            if (!GasHelper.matches(gasInput, inputGas))
                continue;
            if (!StackHelper.matches(itemOutput, outputItem))
                continue;
            if (!GasHelper.matches(gasOutput, outputGas))
                continue;

            recipes.put(entry.getKey(), entry.getValue());
        }

        if (!recipes.isEmpty())
        {
            CraftTweakerAPI.apply(new RemoveMekanismRecipe(NAME, RecipeHandler.Recipe.PRESSURIZED_REACTION_CHAMBER.get(), recipes));
        }
        else
        {
            LogHelper.logWarning(String.format("No %s recipe found for %s, %s, %s, %s and %s. Command ignored!", NAME, itemOutput.toString(), gasOutput.toString(), itemInput.toString(), liquidInput.toString(), gasInput.toString()));
        }
    }

}
