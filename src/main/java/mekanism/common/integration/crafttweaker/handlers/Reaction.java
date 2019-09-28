package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.reaction")
public class Reaction {

    public static final String NAME = Mekanism.MOD_NAME + " Reaction";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IIngredient ingredientInput, ILiquidStack liquidInput, IGasStack gasInput, IItemStack itemOutput, IGasStack gasOutput, double energy, int duration) {
        if (IngredientHelper.checkNotNull(NAME, ingredientInput, liquidInput, gasInput, itemOutput, gasOutput)) {
            CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER,
                  new PressurizedReactionRecipe(IngredientHelper.toIngredient(ingredientInput), IngredientHelper.toIngredient(liquidInput),
                        GasHelper.toGasStackIngredient(gasInput), GasHelper.toGas(gasOutput), energy, duration, IngredientHelper.getItemStack(itemOutput))));
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, IIngredient gasOutput, @ZenCodeType.Optional IIngredient itemInput, @ZenCodeType.Optional IIngredient liquidInput,
          @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput, gasOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER, new IngredientWrapper(itemOutput, gasOutput),
                  new IngredientWrapper(itemInput, liquidInput, gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.PRESSURIZED_REACTION_CHAMBER));
    }*/
}