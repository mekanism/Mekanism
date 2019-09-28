package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.separator")
public class Separator {

    public static final String NAME = Mekanism.MOD_NAME + " Separator";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(ILiquidStack liquidInput, double energy, IGasStack leftGasOutput, IGasStack rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput, leftGasOutput, rightGasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR,
                  new ElectrolysisRecipe(IngredientHelper.toIngredient(liquidInput), energy, GasHelper.toGas(leftGasOutput),
                        GasHelper.toGas(rightGasOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient liquidInput, @ZenCodeType.Optional IIngredient leftGasOutput, @ZenCodeType.Optional IIngredient rightGasOutput) {
        if (IngredientHelper.checkNotNull(NAME, liquidInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR, new IngredientWrapper(leftGasOutput, rightGasOutput),
                  new IngredientWrapper(liquidInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.ELECTROLYTIC_SEPARATOR));
    }*/
}