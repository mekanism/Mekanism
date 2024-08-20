package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CHEMICAL_TO_CHEMICAL)
public class CrTChemicalToChemicalRecipe {

    private CrTChemicalToChemicalRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static ChemicalStackIngredient getInput(ChemicalToChemicalRecipe _this) {
        return _this.getInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<ICrTChemicalStack> getOutputs(ChemicalToChemicalRecipe _this) {
        return CrTUtils.convertChemical(_this.getOutputDefinition());
    }
}