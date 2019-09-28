package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.solar_neutron_activator")
public class SolarNeutronActivator {

    public static final String NAME = Mekanism.MOD_NAME + " Solar Neutron Activator";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IGasStack gasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput, gasOutput)) {
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(gasInput);
            if (gasStackIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.SOLAR_NEUTRON_ACTIVATOR,
                      new GasToGasRecipe(gasStackIngredient, GasHelper.toGas(gasOutput))));
            }
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
    }*/
}