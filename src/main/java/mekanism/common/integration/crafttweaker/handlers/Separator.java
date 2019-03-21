package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
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
import mekanism.common.recipe.machines.SeparatorRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.separator")
@ZenRegister
public class Separator {

    public static final String NAME = Mekanism.MOD_NAME + " Separator";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, double energy, IGasStack leftGasOutput,
          IGasStack rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput, leftGasOutput, rightGasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR,
                        new SeparatorRecipe(IngredientHelper.toFluid(liquidInput), energy,
                              GasHelper.toGas(leftGasOutput), GasHelper.toGas(rightGasOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient liquidInput, @Optional IIngredient leftGasOutput,
          @Optional IIngredient rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR,
                        new IngredientWrapper(leftGasOutput, rightGasOutput), new IngredientWrapper(liquidInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR));
    }
}