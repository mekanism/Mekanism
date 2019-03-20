package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
import com.blamejared.mtlib.helpers.LogHelper;
import com.blamejared.mtlib.helpers.StackHelper;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.mekanism.thermalevaporation")
@ModOnly("mtlib")
@ZenRegister
public class ThermalEvaporation
{
    public static final String NAME = "Mekanism Solar Evaporation";

    @ZenMethod
    public static void addRecipe(ILiquidStack liquidInput, ILiquidStack liquidOutput) {
        if(liquidInput == null || liquidOutput == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        ThermalEvaporationRecipe recipe = new ThermalEvaporationRecipe(InputHelper.toFluid(liquidInput), InputHelper.toFluid(liquidOutput));

        CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, RecipeHandler.Recipe.THERMAL_EVAPORATION_PLANT.get(), recipe));
    }

    @ZenMethod
    public static void removeRecipe(IIngredient liquidInput, @Optional IIngredient liquidOutput) {
        if(liquidInput == null) {
            LogHelper.logError(String.format("Required parameters missing for %s Recipe.", NAME));
            return;
        }

        if(liquidOutput == null) liquidOutput = IngredientAny.INSTANCE;

        CrafttweakerIntegration.LATE_REMOVALS.add(new Remove(NAME, RecipeHandler.Recipe.THERMAL_EVAPORATION_PLANT.get(), liquidInput, liquidOutput));
    }

    private static class Remove extends RemoveMekanismRecipe<FluidInput, ThermalEvaporationRecipe>
    {
        private IIngredient liquidInput;
        private IIngredient liquidOutput;

        public Remove(String name, Map<FluidInput, ThermalEvaporationRecipe> map, IIngredient liquidInput, IIngredient liquidOutput)
        {
            super(name, map);
            this.liquidInput = liquidInput;
            this.liquidOutput = liquidOutput;
        }

        @Override
        public void addRecipes()
        {
            Map<FluidInput, ThermalEvaporationRecipe> recipesToRemove = new HashMap<>();

            for(Map.Entry<FluidInput, ThermalEvaporationRecipe> entry : RecipeHandler.Recipe.THERMAL_EVAPORATION_PLANT.get().entrySet() ) {
                ILiquidStack inputLiquid = InputHelper.toILiquidStack(entry.getKey().ingredient);
                ILiquidStack outputLiquid = InputHelper.toILiquidStack(entry.getValue().recipeOutput.output);

                if(!StackHelper.matches(liquidInput, inputLiquid)) continue;
                if(!StackHelper.matches(liquidOutput, outputLiquid)) continue;

                recipesToRemove.put(entry.getKey(), entry.getValue());
            }

            if(!recipesToRemove.isEmpty()) {
                recipes.putAll(recipesToRemove);
            } else {
                LogHelper.logInfo(String.format("No %s recipe found for %s and %s. Command ignored!", NAME, liquidInput.toString(), liquidOutput.toString()));
            }
        }
    }
}
