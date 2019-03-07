package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.recipe.outputs.FluidOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.thermalevaporation")
@ModOnly("mtlib")
@ZenRegister
public class ThermalEvaporation {
    public static final String NAME = "Mekanism Solar Evaporation";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, ILiquidStack liquidOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput, liquidOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.THERMAL_EVAPORATION_PLANT,
                    new ThermalEvaporationRecipe(InputHelper.toFluid(liquidInput), InputHelper.toFluid(liquidOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient liquidInput, @Optional IIngredient liquidOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<FluidInput, FluidOutput, ThermalEvaporationRecipe>(NAME,
                    RecipeHandler.Recipe.THERMAL_EVAPORATION_PLANT, new IngredientWrapper(liquidOutput), new IngredientWrapper(liquidInput)));
        }
    }
}