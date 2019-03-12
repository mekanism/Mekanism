package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.purification")
@ModOnly("mtlib")
@ZenRegister
public class Purification {

    public static final String NAME = Mekanism.MOD_NAME + " Purification";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, Recipe.PURIFICATION_CHAMBER,
                        new PurificationRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput))));
        }
    }

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, gasInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, Recipe.PURIFICATION_CHAMBER,
                        new PurificationRecipe(new AdvancedMachineInput(InputHelper.toStack(itemInput),
                              GasHelper.toGas(gasInput).getGas()),
                              new ItemStackOutput(InputHelper.toStack(itemOutput)))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput,
          @Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<AdvancedMachineInput, ItemStackOutput, PurificationRecipe>(NAME,
                        Recipe.PURIFICATION_CHAMBER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput, gasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS
              .add(new RemoveAllMekanismRecipe<PurificationRecipe>(NAME, Recipe.PURIFICATION_CHAMBER));
    }
}