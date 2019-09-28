package mekanism.common.integration.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.common.Mekanism;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.chemical.crystallizer")
public class ChemicalCrystallizer {

    public static final String NAME = Mekanism.MOD_NAME + " Chemical Crystallizer";

    //TODO: CrT Integration
    /*@ZenCodeType.Method
    public static void addRecipe(IGasStack gasInput, IItemStack itemOutput) {
        if (IngredientHelper.checkNotNull(NAME, gasInput, itemOutput)) {
            GasStackIngredient gasStackIngredient = GasHelper.toGasStackIngredient(gasInput);
            if (gasStackIngredient != null) {
                CrafttweakerIntegration.LATE_ADDITIONS.add(new AddMekanismRecipe<>(NAME, Recipe.CHEMICAL_CRYSTALLIZER,
                      new GasToItemStackRecipe(gasStackIngredient, IngredientHelper.getItemStack(itemOutput))));
            }
        }
    }

    @ZenCodeType.Method
    public static void removeRecipe(IIngredient itemOutput, @ZenCodeType.Optional IIngredient gasInput) {
        if (IngredientHelper.checkNotNull(NAME, itemOutput)) {
            CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveMekanismRecipe<>(NAME, Recipe.CHEMICAL_CRYSTALLIZER, new IngredientWrapper(itemOutput),
                  new IngredientWrapper(gasInput)));
        }
    }

    @ZenCodeType.Method
    public static void removeAllRecipes() {
        CrafttweakerIntegration.LATE_REMOVALS.add(new RemoveAllMekanismRecipe<>(NAME, Recipe.CHEMICAL_CRYSTALLIZER));
    }*/
}