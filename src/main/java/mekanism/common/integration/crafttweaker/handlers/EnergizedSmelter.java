package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.mtlib.helpers.InputHelper;
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
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.smelter")
@ModOnly("mtlib")
@ZenRegister
public class EnergizedSmelter {

    public static final String NAME = "Mekanism Smelter";
    private static boolean removedRecipe = false;
    private static boolean addedRecipe = false;

    public static boolean hasRemovedRecipe() {
        return removedRecipe;
    }

    public static boolean hasAddedRecipe() {
        return addedRecipe;
    }

    @ZenMethod
    public static void addRecipe(IItemStack itemInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe(NAME, RecipeHandler.Recipe.ENERGIZED_SMELTER,
                        new SmeltingRecipe(InputHelper.toStack(itemInput), InputHelper.toStack(itemOutput))));
            addedRecipe = true;
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemInput, @Optional IIngredient itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<ItemStackInput, ItemStackOutput, SmeltingRecipe>(NAME,
                        RecipeHandler.Recipe.ENERGIZED_SMELTER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput)));
            removedRecipe = true;
        }
    }
}