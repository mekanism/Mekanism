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
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.purification")
@ModOnly("mtlib")
@ZenRegister
public class Purification
{
    public static final String NAME = "Mekanism Purification";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput)
    {
        if (itemInput == null || itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        PurificationRecipe recipe = new PurificationRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput));

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IGasStack gasInput, IItemStack itemOutput)
    {
        if (itemInput == null || gasInput == null || itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        AdvancedMachineInput input = new AdvancedMachineInput(InputHelper.toStack(itemInput), GasHelper.toGas(gasInput).getGas());
        ItemStackOutput output = new ItemStackOutput(InputHelper.toStack(itemOutput));

        PurificationRecipe recipe = new PurificationRecipe(input, output);

        CraftTweakerAPI.apply(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional IIngredient gasInput)
    {
        if (itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (itemInput == null)
            itemInput = IngredientAny.INSTANCE;
        if (gasInput == null)
            gasInput = IngredientAny.INSTANCE;

        Map<MachineInput, MachineRecipe> recipes = new HashMap<>();

        for (Map.Entry<AdvancedMachineInput, PurificationRecipe> entry : ((Map<AdvancedMachineInput, PurificationRecipe>) RecipeHandler.Recipe.PURIFICATION_CHAMBER.get()).entrySet())
        {
            IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().itemStack);
            IGasStack inputGas = new CraftTweakerGasStack(new GasStack(entry.getKey().gasType, 1));
            IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().recipeOutput.output);

            if (!StackHelper.matches(itemOutput, outputItem))
                continue;
            if (!StackHelper.matches(itemInput, inputItem))
                continue;
            if (!GasHelper.matches(gasInput, inputGas))
                continue;

            recipes.put(entry.getKey(), entry.getValue());
        }

        if (!recipes.isEmpty())
        {
            CraftTweakerAPI.apply(new RemoveMekanismRecipe(NAME, RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), recipes));
        }
        else
        {
            LogHelper.logWarning(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME, itemOutput.toString(), itemInput.toString(), gasInput.toString()));
        }
    }
}
