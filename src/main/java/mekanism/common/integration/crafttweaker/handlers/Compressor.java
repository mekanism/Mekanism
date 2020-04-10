package mekanism.common.integration.crafttweaker.handlers;

import mekanism.common.Mekanism;

//@ZenRegister
//@ZenCodeType.Name("mekanism.compressor")
public class Compressor {

    public static final String NAME = Mekanism.MOD_NAME + " Compressor";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
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
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(gasInput);
            if (gasStackIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.OSMIUM_COMPRESSOR,
                      new ItemStackGasToItemStackRecipe(IngredientHelper.toIngredient(ingredientInput), gasStackIngredient,
                            IngredientHelper.getItemStack(itemOutput))));
            }
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
    }*/
}