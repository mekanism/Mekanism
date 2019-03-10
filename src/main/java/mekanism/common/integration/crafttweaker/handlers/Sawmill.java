package mekanism.common.integration.crafttweaker.handlers;

import static com.blamejared.mtlib.helpers.InputHelper.toStack;

import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.sawmill")
@ModOnly("mtlib")
@ZenRegister
public class Sawmill {

    public static final String NAME = "Mekanism Sawmill";

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput, @Optional IItemStack optionalItemOutput,
          @Optional double optionalChance) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.PRECISION_SAWMILL,
                        new SawmillRecipe(new ItemStackInput(toStack(itemInput)),
                              optionalItemOutput == null ? new ChanceOutput(toStack(itemOutput))
                                    : new ChanceOutput(toStack(itemOutput), toStack(optionalItemOutput),
                                          optionalChance))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemInput, @Optional IIngredient itemOutput,
          @Optional IIngredient optionalItemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<ItemStackInput, ChanceOutput, SawmillRecipe>(NAME,
                        RecipeHandler.Recipe.PRECISION_SAWMILL, new IngredientWrapper(itemInput),
                        new IngredientWrapper(itemOutput, optionalItemOutput)));
        }
    }
}