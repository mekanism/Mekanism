package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.combiner")
@ZenRegister
public class Combiner {

    public static final String NAME = Mekanism.MOD_NAME + " Combiner";

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IIngredient ingredientExtra, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, ingredientExtra, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER,
                  new CombinerRecipe(CraftTweakerMC.getIngredient(ingredientInput), CraftTweakerMC.getIngredient(ingredientExtra), CraftTweakerMC.getItemStack(itemOutput))));
        }
    }

    /**
     * @deprecated Replaced by {@link #addRecipe(IIngredient, IIngredient, IItemStack)}. May be removed with Minecraft 1.13.
     */
    @ZenMethod
    @Deprecated
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER,
                  new CombinerRecipe(CraftTweakerMC.getIngredient(ingredientInput), Ingredient.fromStacks(new ItemStack(Blocks.COBBLESTONE)), CraftTweakerMC.getItemStack(itemOutput))));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional IIngredient extraInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.COMBINER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, extraInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.COMBINER));
    }
}