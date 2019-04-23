package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
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
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.chemical.infuser")
@ZenRegister
public class ChemicalInfuser {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Infuser";

    @ZenMethod
    public static void addRecipe(IGasStack leftGasInput, IGasStack rightGasInput, IGasStack gasOutput) {
        if (IngredientHelper.checkNotNull(NAME, leftGasInput, rightGasInput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER,
                        new ChemicalInfuserRecipe(GasHelper.toGas(leftGasInput), GasHelper.toGas(rightGasInput),
                              GasHelper.toGas(gasOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient gasOutput, @Optional IIngredient leftGasInput,
          @Optional IIngredient rightGasInput) {
        if (IngredientHelper.checkNotNull(NAME, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER, new IngredientWrapper(gasOutput),
                        new IngredientWrapper(leftGasInput, rightGasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_INFUSER));
    }
}