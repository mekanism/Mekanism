package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.combiner")
@ModOnly("mtlib")
@ZenRegister
public class Combiner
{
    public static final String NAME = "Mekanism Combiner";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack extraInput, IItemStack itemOutput)
    {
        if (itemInput == null || extraInput == null || itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        DoubleMachineInput input = new DoubleMachineInput(InputHelper.toStack(itemInput), InputHelper.toStack(extraInput));
        ItemStackOutput output = new ItemStackOutput(InputHelper.toStack(itemOutput));
        CombinerRecipe recipe = new CombinerRecipe(input, output);

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.COMBINER.get(), recipe));
    }

    /**
     * @deprecated Replaced by {@link #addRecipe(IItemStack, IItemStack, IItemStack)}.
     * May be removed with Minecraft 1.13.
     */
    @ZenMethod
    @Deprecated
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput)
    {
        if (itemInput == null || itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        CombinerRecipe recipe = new CombinerRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput));

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.COMBINER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional IIngredient gasInput)
    {
        if (itemOutput == null)
        {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (gasInput == null)
            gasInput = IngredientAny.INSTANCE;
        if (itemInput == null)
            itemInput = IngredientAny.INSTANCE;

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.COMBINER.get(), itemOutput, itemInput, gasInput));
    }

    private static class Remove extends RemoveMekanismRecipe
    {
        private IIngredient itemOutput;
        private IIngredient itemInput;
        private IIngredient itemExtra;

        public Remove(String name, Map<MachineInput, MachineRecipe> map, IIngredient itemOutput, IIngredient itemInput, IIngredient extraInput)
        {
            super(name, map);

            this.itemOutput = itemOutput;
            this.itemInput = itemInput;
            this.itemExtra = extraInput;
        }

        @Override
        public void addRecipes()
        {
            Map<MachineInput, MachineRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<DoubleMachineInput, CombinerRecipe> entry : ((Map<DoubleMachineInput, CombinerRecipe>) RecipeHandler.Recipe.COMBINER.get()).entrySet())
            {
                IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().itemStack);
                IItemStack extraItem = InputHelper.toIItemStack(entry.getKey().extraStack);
                IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().getOutput().output);

                if (!StackHelper.matches(itemInput, inputItem))
                    continue;
                if (!StackHelper.matches(itemExtra, extraItem))
                    continue;
                if (!StackHelper.matches(itemOutput, outputItem))
                    continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty())
            {
                recipes.putAll(recipesToRemove);
            }
            else
            {
                LogHelper.logWarning(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME, itemInput.toString(), itemExtra.toString(), itemOutput.toString()));
            }
        }
    }
}
