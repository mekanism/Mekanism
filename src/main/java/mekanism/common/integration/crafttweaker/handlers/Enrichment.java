package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
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
@ZenCodeType.Name("mekanism.enrichment")
public class Enrichment {

    public static final String NAME = Mekanism.MOD_NAME + " Enrichment Chamber";

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = IngredientHelper.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.ENRICHMENT_CHAMBER,
                  new ItemStackToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), output)));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemInput, @ZenCodeType.Optional IIngredient itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.ENRICHMENT_CHAMBER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.ENRICHMENT_CHAMBER));
    }
}