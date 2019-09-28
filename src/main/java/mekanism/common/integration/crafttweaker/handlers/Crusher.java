package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.crusher")
public class Crusher {

    public static final String NAME = Mekanism.MOD_NAME + " Crusher";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = IngredientHelper.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CRUSHER,
                  new ItemStackToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), output)));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient itemInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CRUSHER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CRUSHER));
    }*/
}