package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalDissolutionRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class CrTChemicalDissolutionRecipe {

    private CrTChemicalDissolutionRecipe() {
    }

    /**
     * Gets the input item ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("itemInput")
    public static ItemStackIngredient getItemInput(ChemicalDissolutionRecipe _this) {
        return _this.getItemInput();
    }

    /**
     * Gets the input gas ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("gasInput")
    public static GasStackIngredient getGasInput(ChemicalDissolutionRecipe _this) {
        return _this.getGasInput();
    }

    //TODO - 1.18: Outputs after we figure out how we are adjusting them
}