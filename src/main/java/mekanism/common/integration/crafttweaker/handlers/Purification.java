package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
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
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.purification")
@ZenRegister
public class Purification {

    public static final String NAME = Mekanism.MOD_NAME + " Purification";

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = CraftTweakerMC.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER,
                  new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), GasStackIngredient.from(MekanismFluids.Oxygen, 1), output)));
        }
    }

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasInput, itemOutput)) {
            ItemStack output = CraftTweakerMC.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER,
                  new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), GasHelper.toGasStackIngredient(gasInput), output)));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput, @Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, gasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER));
    }
}