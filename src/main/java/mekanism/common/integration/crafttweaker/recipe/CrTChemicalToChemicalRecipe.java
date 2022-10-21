package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getInput", parameters = {}, getterName = "input")
@NativeTypeRegistration(value = ChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CHEMICAL_TO_CHEMICAL)
public class CrTChemicalToChemicalRecipe {

    private CrTChemicalToChemicalRecipe() {
    }

    @ZenRegister
    @NativeTypeRegistration(value = GasToGasRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_GAS_TO_GAS)
    public static class CrTGasToGasRecipe {

        private CrTGasToGasRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTGasStack> getOutputs(GasToGasRecipe _this) {
            return CrTUtils.convertGas(_this.getOutputDefinition());
        }
    }
}