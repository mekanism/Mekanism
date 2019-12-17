package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.chemical.infuser")
public class ChemicalInfuser {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Infuser";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IGasStack leftGasInput, IGasStack rightGasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, leftGasInput, rightGasInput, gasOutput)) {
            GasStackIngredient leftGasIngredient = GasHelper.toGasStackIngredient(leftGasInput);
            GasStackIngredient rightGasIngredient = GasHelper.toGasStackIngredient(rightGasInput);
            if (leftGasIngredient != null && rightGasIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER,
                      new ChemicalInfuserRecipe(leftGasIngredient, rightGasIngredient, GasHelper.toGas(gasOutput))));
            }
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient gasOutput, @ZenCodeType.Optional IIngredient leftGasInput, @ZenCodeType.Optional IIngredient rightGasInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(leftGasInput, rightGasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER));
    }*/
}