package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
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

@ZenClass("mods.mekanism.purification")
@ModOnly("mtlib")
@ZenRegister
public class Purification {

    public static final String NAME = "Mekanism Purification";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput) {
        if (itemInput == null || itemOutput == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        PurificationRecipe recipe = new PurificationRecipe(InputHelper.toStack(itemInput),
              InputHelper.toStack(itemOutput));

        CrafttweakerIntegration.LATE_ADDITIONS
              .add(new AddMekanismRecipe<>(NAME, RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IGasStack gasInput, IItemStack itemOutput) {
        if (itemInput == null || gasInput == null || itemOutput == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        AdvancedMachineInput input = new AdvancedMachineInput(InputHelper.toStack(itemInput),
              GasHelper.toGas(gasInput).getGas());
        ItemStackOutput output = new ItemStackOutput(InputHelper.toStack(itemOutput));

        PurificationRecipe recipe = new PurificationRecipe(input, output);

        CrafttweakerIntegration.LATE_ADDITIONS
              .add(new AddMekanismRecipe<>(NAME, RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput,
          @Optional IIngredient gasInput) {
        if (itemOutput == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if (itemInput == null) {
            itemInput = IngredientAny.INSTANCE;
        }
        if (gasInput == null) {
            gasInput = IngredientAny.INSTANCE;
        }

        CrafttweakerIntegration.LATE_REMOVALS
              .add(new Remove(NAME, (Map) RecipeHandler.Recipe.PURIFICATION_CHAMBER.get(), itemOutput, itemInput,
                    gasInput));
    }

    private static class Remove extends RemoveMekanismRecipe {

        private IIngredient itemOutput;
        private IIngredient itemInput;
        private IIngredient gasInput;

        public Remove(String name, Map<MachineInput, MachineRecipe> map, IIngredient itemOutput, IIngredient itemInput,
              IIngredient gasInput) {
            super(name, map);

            this.itemOutput = itemOutput;
            this.itemInput = itemInput;
            this.gasInput = gasInput;
        }

        @Override
        public void addRecipes() {
            Map<MachineInput, MachineRecipe> recipesToRemove = new HashMap<>();

            for (Map.Entry<AdvancedMachineInput, PurificationRecipe> entry : RecipeHandler.Recipe.PURIFICATION_CHAMBER
                  .get().entrySet()) {
                IItemStack inputItem = InputHelper.toIItemStack(entry.getKey().itemStack);
                IGasStack inputGas = new CraftTweakerGasStack(new GasStack(entry.getKey().gasType, 1));
                IItemStack outputItem = InputHelper.toIItemStack(entry.getValue().recipeOutput.output);

                if (!StackHelper.matches(itemOutput, outputItem)) {
                    continue;
                }
                if (!StackHelper.matches(itemInput, inputItem)) {
                    continue;
                }
                if (!GasHelper.matches(gasInput, inputGas)) {
                    continue;
                }

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if (!recipesToRemove.isEmpty()) {
                recipes.putAll(recipesToRemove);
            } else {
                LogHelper.logInfo(String.format("No %s recipe found for %s, %s and %s. Command ignored!", NAME,
                      itemOutput.toString(), itemInput.toString(), gasInput.toString()));
            }
        }
    }
}
