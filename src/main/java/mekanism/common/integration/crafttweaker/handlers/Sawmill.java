package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.sawmill")
@ZenRegister
public class Sawmill {

    public static final String NAME = Mekanism.MOD_NAME + " Sawmill";

    //TODO: Make this be two methods to make sure optional chance is not optional if there is a secondary output?
    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput, @Optional IItemStack optionalItemOutput, @Optional double optionalChance) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            Ingredient input = CraftTweakerMC.getIngredient(ingredientInput);
            ItemStack output = CraftTweakerMC.getItemStack(itemOutput);
            SawmillRecipe recipe;
            if (optionalItemOutput == null) {
                recipe = new SawmillRecipe(input, output, ItemStack.EMPTY, 0);
            } else {
                recipe = new SawmillRecipe(input, output, CraftTweakerMC.getItemStack(optionalItemOutput), optionalChance);
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL, recipe));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemInput, @Optional IIngredient itemOutput, @Optional IIngredient optionalItemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL, new IngredientWrapper(itemOutput, optionalItemOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL));
    }
}