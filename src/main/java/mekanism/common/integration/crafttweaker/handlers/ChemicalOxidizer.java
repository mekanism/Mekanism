package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.chemical.oxidizer")
public class ChemicalOxidizer {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Oxidizer";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_OXIDIZER,
                  new ItemStackToGasRecipe(IngredientHelper.toIngredient(ingredientInput), GasHelper.toGas(gasOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient gasOutput, @ZenCodeType.Optional IIngredient itemInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_OXIDIZER, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_OXIDIZER));
    }*/
}