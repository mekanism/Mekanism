package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getInput", parameters = {}, getterName = "input")
@NativeTypeRegistration(value = ChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CHEMICAL_TO_CHEMICAL)
public class CrTChemicalToChemicalRecipe {

    private CrTChemicalToChemicalRecipe() {
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