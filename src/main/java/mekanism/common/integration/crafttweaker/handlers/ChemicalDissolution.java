package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
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
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.chemical.dissolution")
public class ChemicalDissolution {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Dissolution Chamber";

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IGasStack inputGas, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, inputGas, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER,
                  new ItemStackGasToGasRecipe(IngredientHelper.toIngredient(ingredientInput), GasHelper.toGasStackIngredient(inputGas), GasHelper.toGas(gasOutput))));
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
    }
}