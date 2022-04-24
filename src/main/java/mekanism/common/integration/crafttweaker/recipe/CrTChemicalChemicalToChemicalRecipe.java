package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getLeftInput", parameters = {}, getterName = "leftInput")
@NativeMethod(name = "getRightInput", parameters = {}, getterName = "rightInput")
@NativeTypeRegistration(value = ChemicalChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CHEMICAL_CHEMICAL_TO_CHEMICAL)
public class CrTChemicalChemicalToChemicalRecipe {

    private CrTChemicalChemicalToChemicalRecipe() {
    }

    @ZenRegister
    @NativeTypeRegistration(value = ChemicalInfuserRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CHEMICAL_INFUSING)
    public static class CrTChemicalInfuserRecipe {

        private CrTChemicalInfuserRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTGasStack> getOutputs(ChemicalInfuserRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTGasStack::new);
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = PigmentMixingRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_PIGMENT_MIXING)
    public static class CrTPigmentMixingRecipe {

        private CrTPigmentMixingRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTPigmentStack> getOutputs(PigmentMixingRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTPigmentStack::new);
        }
    }
}