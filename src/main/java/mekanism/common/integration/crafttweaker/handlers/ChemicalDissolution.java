package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.chemical.dissolution")
public class ChemicalDissolution {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Dissolution Chamber";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IGasStack inputGas, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, inputGas, gasOutput)) {
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(inputGas);
            if (gasStackIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER,
                      new ItemStackGasToGasRecipe(IngredientHelper.toIngredient(ingredientInput), gasStackIngredient, GasHelper.toGas(gasOutput))));
            }
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient gasOutput, @ZenCodeType.Optional IIngredient itemInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER));
    }*/
}