package mekanism.common.integration.crafttweaker.handlers;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.item.ItemStack;
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
            ItemStackOutput output = new ItemStackOutput(CraftTweakerMC.getItemStack(itemOutput));
            List<CombinerRecipe> recipes = new ArrayList<>();
            ItemStack[] extraInputs = CraftTweakerMC.getIngredient(ingredientExtra).getMatchingStacks();
            for (ItemStack stack : CraftTweakerMC.getIngredient(ingredientInput).getMatchingStacks()) {
                for (ItemStack extra : extraInputs) {
                    recipes.add(new CombinerRecipe(new DoubleMachineInput(stack, extra), output));
                }
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER, recipes));
        }
    }

    /**
     * @deprecated Replaced by {@link #addRecipe(IIngredient, IIngredient, IItemStack)}. May be removed with Minecraft
     * 1.13.
     */
    @ZenMethod
    @Deprecated
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = CraftTweakerMC.getItemStack(itemOutput);
            List<CombinerRecipe> recipes = new ArrayList<>();
            for (ItemStack stack : CraftTweakerMC.getIngredient(ingredientInput).getMatchingStacks()) {
                recipes.add(new CombinerRecipe(stack, output));
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER, recipes));
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