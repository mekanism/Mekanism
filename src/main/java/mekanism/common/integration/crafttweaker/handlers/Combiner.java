package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.combiner")
public class Combiner {

    public static final String NAME = Mekanism.MOD_NAME + " Combiner";

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IIngredient ingredientExtra, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, ingredientExtra, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.COMBINER,
                  new CombinerRecipe(IngredientHelper.toIngredient(ingredientInput), IngredientHelper.toIngredient(ingredientExtra),
                        IngredientHelper.getItemStack(itemOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional IIngredient extraInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.COMBINER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, extraInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.COMBINER));
    }
}