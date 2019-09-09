package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.chemical.dissolution")
@ZenRegister
public class ChemicalDissolution {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Dissolution Chamber";

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER,
                  new ItemStackGasToGasRecipe(IngredientHelper.toIngredient(ingredientInput), GasIngredient.fromInstance(MekanismFluids.SulfuricAcid), GasHelper.toGas(gasOutput))));
        }
    }

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IGasStack inputGas, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, inputGas, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER,
                  new ItemStackGasToGasRecipe(IngredientHelper.toIngredient(ingredientInput), GasHelper.toGasIngredient(inputGas), GasHelper.toGas(gasOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasOutput, @Optional IIngredient itemInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, new IngredientWrapper(gasOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_DISSOLUTION_CHAMBER));
    }
}