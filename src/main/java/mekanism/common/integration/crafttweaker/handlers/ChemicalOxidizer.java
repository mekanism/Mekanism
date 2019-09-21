package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import mekanism.api.recipes.ItemStackToGasRecipe;
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
@ZenCodeType.Name("mekanism.chemical.oxidizer")
public class ChemicalOxidizer {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Oxidizer";

    @ZenCodeType.Method
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
    }
}