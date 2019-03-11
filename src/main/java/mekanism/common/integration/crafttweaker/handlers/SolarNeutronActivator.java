package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.recipe.outputs.GasOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.solarneutronactivator")
@ModOnly("mtlib")
@ZenRegister
public class SolarNeutronActivator {

    public static final String NAME = "Mekanism Solar Neutron Activator";

    @ZenMethod
    public static void addRecipe(IGasStack gasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR,
                        new SolarNeutronRecipe(GasHelper.toGas(gasInput), GasHelper.toGas(gasOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasInput, @Optional IIngredient gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<GasInput, GasOutput, SolarNeutronRecipe>(NAME,
                        Recipe.SOLAR_NEUTRON_ACTIVATOR, new IngredientWrapper(gasOutput),
                        new IngredientWrapper(gasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS
              .add(new RemoveAllMekanismRecipe<SolarNeutronRecipe>(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR));
    }
}