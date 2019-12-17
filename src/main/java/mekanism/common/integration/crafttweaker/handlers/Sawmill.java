package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.sawmill")
public class Sawmill {

    public static final String NAME = Mekanism.MOD_NAME + " Sawmill";

    //TODO: Make this be two methods to make sure optional chance is not optional if there is a secondary output?
    //TODO: CrT Integration
    /*@ZenCodeType.Method
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
    }*/
}