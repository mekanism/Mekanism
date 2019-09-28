package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.thermal_evaporation")
public class ThermalEvaporation {

    public static final String NAME = Mekanism.MOD_NAME + " Solar Evaporation";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(ILiquidStack liquidInput, ILiquidStack liquidOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput, liquidOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.THERMAL_EVAPORATION_PLANT,
                  new FluidToFluidRecipe(IngredientHelper.toIngredient(liquidInput), IngredientHelper.toFluid(liquidOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient liquidInput, @ZenCodeType.Optional IIngredient liquidOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.THERMAL_EVAPORATION_PLANT, new IngredientWrapper(liquidOutput),
                  new IngredientWrapper(liquidInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.THERMAL_EVAPORATION_PLANT));
    }*/
}