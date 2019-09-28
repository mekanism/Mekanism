package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.smelter")
public class EnergizedSmelter {

    public static final String NAME = Mekanism.MOD_NAME + " Smelter";
    private static boolean removedRecipe = false;
    private static boolean addedRecipe = false;

    public static boolean hasRemovedRecipe() {
        return removedRecipe;
    }

    public static boolean hasAddedRecipe() {
        return addedRecipe;
    }

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, itemOutput)) {
            ItemStack output = IngredientHelper.getItemStack(itemOutput);
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.ENERGIZED_SMELTER,
                  new ItemStackToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), output)));
            addedRecipe = true;
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemInput, @ZenCodeType.Optional IIngredient itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, itemInput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.ENERGIZED_SMELTER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(itemInput)));
            removedRecipe = true;
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.ENERGIZED_SMELTER));
    }*/
}