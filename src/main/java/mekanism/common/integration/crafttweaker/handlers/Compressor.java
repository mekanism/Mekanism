package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismGases;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.helpers.GasHelper;
import mekanism.common.integration.crafttweaker.helpers.IngredientHelper;
import mekanism.common.integration.crafttweaker.util.AddMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.integration.crafttweaker.util.RemoveAllMekanismRecipe;
import mekanism.common.integration.crafttweaker.util.RemoveMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.compressor")
public class Compressor {

    public static final String NAME = Mekanism.MOD_NAME + " Compressor";

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.OSMIUM_COMPRESSOR,
                  new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
                        IngredientHelper.getItemStack(itemOutput))));
        }
    }

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasInput, itemOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.OSMIUM_COMPRESSOR,
                  new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), GasHelper.toGasStackIngredient(gasInput),
                        IngredientHelper.getItemStack(itemOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.OSMIUM_COMPRESSOR, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.OSMIUM_COMPRESSOR));
    }
}