package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.gas.Gas;
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
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
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
            List<PurificationRecipe> recipes = new ArrayList<>();
            for (ItemStack stack : CraftTweakerMC.getIngredient(ingredientInput).getMatchingStacks()) {
                recipes.add(new PurificationRecipe(stack, output));
            }
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER, recipes));
        }
    }

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasInput, itemOutput)) {
            Gas gas = GasHelper.toGas(gasInput).getGas();
            ItemStackOutput output = new ItemStackOutput(CraftTweakerMC.getItemStack(itemOutput));
            List<PurificationRecipe> recipes = new ArrayList<>();
            for (ItemStack stack : CraftTweakerMC.getIngredient(ingredientInput).getMatchingStacks()) {
                recipes.add(new PurificationRecipe(new AdvancedMachineInput(stack, gas), output));
            }
            CrafttweakerIntegration.LATE_ADDITIONS
                  .add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER, recipes));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput,
          @Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput, gasInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER));
    }
}