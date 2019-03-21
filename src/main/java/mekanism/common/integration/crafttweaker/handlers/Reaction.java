package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
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
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.outputs.PressurizedOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.reaction")
@ZenRegister
public class Reaction {

    public static final String NAME = Mekanism.MOD_NAME + " Reaction";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, ILiquidStack liquidInput, IGasStack gasInput,
          IItemStack itemOutput, IGasStack gasOutput, double energy, int duration) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, liquidInput, gasInput, itemOutput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER,
                        new PressurizedRecipe(
                              new PressurizedInput(IngredientHelper.toStack(itemInput),
                                    IngredientHelper.toFluid(liquidInput), GasHelper.toGas(gasInput)),
                              new PressurizedOutput(IngredientHelper.toStack(itemOutput), GasHelper.toGas(gasOutput)),
                              energy, duration)));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, IIngredient gasOutput, @Optional IIngredient itemInput,
          @Optional IIngredient liquidInput, @Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER,
                        new IngredientWrapper(itemOutput, gasOutput),
                        new IngredientWrapper(itemInput, liquidInput, gasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS
              .add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER));
    }
}