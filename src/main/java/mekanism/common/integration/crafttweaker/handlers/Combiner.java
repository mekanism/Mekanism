package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.combiner")
@ZenRegister
public class Combiner {

    public static final String NAME = Mekanism.MOD_NAME + " Combiner";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack extraInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, extraInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER, new CombinerRecipe(
                        new DoubleMachineInput(IngredientHelper.toStack(itemInput),
                              IngredientHelper.toStack(extraInput)),
                        new ItemStackOutput(IngredientHelper.toStack(itemOutput)))));
        }
    }

    /**
     * @deprecated Replaced by {@link #addRecipe(IItemStack, IItemStack, IItemStack)}. May be removed with Minecraft
     * 1.13.
     */
    @ZenMethod
    @Deprecated
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER,
                  new CombinerRecipe(IngredientHelper.toStack(itemInput), IngredientHelper.toStack(itemOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput,
          @Optional IIngredient extraInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.COMBINER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput, extraInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.COMBINER));
    }
}