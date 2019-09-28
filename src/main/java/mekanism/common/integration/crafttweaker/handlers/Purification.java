package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.purification")
public class Purification {

    public static final String NAME = Mekanism.MOD_NAME + " Purification";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = IngredientHelper.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER,
                  new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), GasStackIngredient.from(MekanismTags.OXYGEN, 1), output)));
        }
    }

    @ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, gasInput, itemOutput)) {
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(gasInput);
            if (gasStackIngredient != null) {
                ItemStack output = IngredientHelper.getItemStack(itemOutput);
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER,
                      new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), gasStackIngredient, output)));
            }
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput, gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PURIFICATION_CHAMBER));
    }*/
}