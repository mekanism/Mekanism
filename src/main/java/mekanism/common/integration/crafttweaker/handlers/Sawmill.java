package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.sawmill")
public class Sawmill {

    public static final String NAME = Mekanism.MOD_NAME + " Sawmill";

    //TODO: Make this be two methods to make sure optional chance is not optional if there is a secondary output?
    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput, @ZenCodeType.Optional IItemStack optionalItemOutput, @ZenCodeType.Optional double optionalChance) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStackIngredient input = IngredientHelper.toIngredient(ingredientInput);
            ItemStack output = IngredientHelper.getItemStack(itemOutput);
            SawmillRecipe recipe;
            if (optionalItemOutput == null) {
                recipe = new SawmillRecipe(input, output, ItemStack.EMPTY, 0);
            } else {
                recipe = new SawmillRecipe(input, output, IngredientHelper.getItemStack(optionalItemOutput), optionalChance);
            }
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL, recipe));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemInput, @ZenCodeType.Optional IIngredient itemOutput, @ZenCodeType.Optional IIngredient optionalItemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL, new IngredientWrapper(itemOutput, optionalItemOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PRECISION_SAWMILL));
    }
}