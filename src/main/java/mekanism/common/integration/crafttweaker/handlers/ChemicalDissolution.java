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
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.chemical.dissolution")
@ModOnly("mtlib")
@ZenRegister
public class ChemicalDissolution
{
    public static final String NAME = "Mekanism Chemical Dissolution Chamber";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IGasStack gasOutput)
    {
        if (itemInput == null || gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        DissolutionRecipe recipe = new DissolutionRecipe(InputHelper.toStack(itemInput), GasHelper.toGas(gasOutput));

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasOutput, @Optional IIngredient itemInput)
    {
        if (gasOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (itemInput == null)
            itemInput = IngredientAny.INSTANCE;

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get(), gasOutput, itemInput));
    }

    private static class Remove extends RemoveMekanismRecipe
    {
        private IIngredient gasOutput;
        private IIngredient itemInput;

        public Remove(String name, Map<MachineInput, MachineRecipe> map, IIngredient gasOutput, IIngredient itemInput)
        {
            super(name, map);

            this.gasOutput = gasOutput;
            this.itemInput = itemInput;
        }

        @Override
        public void addRecipes()
        {
            Map<MachineInput, MachineRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<ItemStackInput, DissolutionRecipe> entry : ((Map<ItemStackInput, DissolutionRecipe>) RecipeHandler.Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get()).entrySet())
            {
                IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().ingredient);
                IGasStack outputGas = new CraftTweakerGasStack(entry.getValue().recipeOutput.output);

                if (!GasHelper.matches(gasOutput, outputGas))
                    continue;
                if (!StackHelper.matches(itemInput, inputItem))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logWarning(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, gasOutput.toString(), itemInput.toString()));
            }
        }
    }
}
