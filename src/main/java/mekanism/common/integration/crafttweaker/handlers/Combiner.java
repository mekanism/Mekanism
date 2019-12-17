package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.combiner")
public class Combiner {

    public static final String NAME = Mekanism.MOD_NAME + " Combiner";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
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
    }*/
}