package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.separator")
@ModOnly("mtlib")
@ZenRegister
public class Separator {

    public static final String NAME = "Mekanism Separator";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, double energy, IGasStack leftGasOutput,
          IGasStack rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput, leftGasOutput, rightGasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR,
                        new SeparatorRecipe(InputHelper.toFluid(liquidInput), energy, GasHelper.toGas(leftGasOutput),
                              GasHelper.toGas(rightGasOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient liquidInput, @Optional IIngredient leftGasOutput,
          @Optional IIngredient rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<FluidInput, ChemicalPairOutput, SeparatorRecipe>(NAME,
                        RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR, new IngredientWrapper(liquidInput),
                        new IngredientWrapper(leftGasOutput, rightGasOutput)));
        }
    }
}