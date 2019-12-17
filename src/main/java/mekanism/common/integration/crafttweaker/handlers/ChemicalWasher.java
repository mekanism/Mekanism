package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.chemical.washer")
public class ChemicalWasher {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Washer";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(ILiquidStack fluidInput, IGasStack gasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, fluidInput, gasInput, gasOutput)) {
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(gasInput);
            if (gasStackIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_WASHER,
                      new FluidGasToGasRecipe(IngredientHelper.toIngredient(fluidInput), gasStackIngredient, GasHelper.toGas(gasOutput))));
            }
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient gasOutput, @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_WASHER, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_WASHER));
    }*/
}