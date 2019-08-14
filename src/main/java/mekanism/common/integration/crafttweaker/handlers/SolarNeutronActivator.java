package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
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
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.solar_neutron_activator")
public class SolarNeutronActivator {

    public static final String NAME = Mekanism.MOD_NAME + " Solar Neutron Activator";

    @ZenCodeType.Method
    public static void addRecipe(IGasStack gasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR, new SolarNeutronRecipe(GasHelper.toGas(gasInput),
                  GasHelper.toGas(gasOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient gasInput, @ZenCodeType.Optional IIngredient gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR));
    }
}