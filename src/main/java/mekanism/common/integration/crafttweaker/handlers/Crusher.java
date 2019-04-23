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
import mekanism.common.recipe.machines.CrusherRecipe;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.mekanism.crusher")
@ZenRegister
public class Crusher {

    public static final String NAME = Mekanism.MOD_NAME + " Crusher";

    @ZenMethod
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = CraftTweakerMC.getItemStack(itemOutput);
            List<CrusherRecipe> recipes = new ArrayList<>();
            for (ItemStack stack : CraftTweakerMC.getIngredient(ingredientInput).getMatchingStacks()) {
                recipes.add(new CrusherRecipe(stack, output));
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CRUSHER, recipes));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient itemOutput, @Optional IIngredient itemInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS
                  .add(new RemoveMekanismRecipe<>(NAME, Recipe.CRUSHER, new IngredientWrapper(itemOutput),
                        new IngredientWrapper(itemInput)));
        }
    }

    @ZenMethod
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CRUSHER));
    }
}