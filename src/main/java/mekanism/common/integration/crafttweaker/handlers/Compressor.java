package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.compressor")
@ModOnly("mtlib")
@ZenRegister
public class Compressor {
    public static final String NAME = "Mekanism Compressor";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.OSMIUM_COMPRESSOR,
                    new OsmiumCompressorRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput))));
        }
    }

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, gasInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.OSMIUM_COMPRESSOR,
                    new OsmiumCompressorRecipe(new AdvancedMachineInput(InputHelper.toStack(itemInput), GasHelper.toGas(gasInput).getGas()),
                            new ItemStackOutput(InputHelper.toStack(itemOutput)))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<AdvancedMachineInput, ItemStackOutput, OsmiumCompressorRecipe>(NAME,
                    RecipeHandler.Recipe.OSMIUM_COMPRESSOR, new IngredientWrapper(itemOutput), new IngredientWrapper(itemInput, gasInput)));
        }
    }
}