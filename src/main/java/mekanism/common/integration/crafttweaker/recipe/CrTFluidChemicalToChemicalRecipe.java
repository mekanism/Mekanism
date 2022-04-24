package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getFluidInput", parameters = {}, getterName = "fluidInput")
@NativeMethod(name = "getChemicalInput", parameters = {}, getterName = "chemicalInput")
@NativeTypeRegistration(value = FluidChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_FLUID_CHEMICAL_TO_CHEMICAL)
public class CrTFluidChemicalToChemicalRecipe {

    private CrTFluidChemicalToChemicalRecipe() {
    }

    @ZenRegister
    @NativeTypeRegistration(value = FluidSlurryToSlurryRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_FLUID_SLURRY_TO_SLURRY)
    public static class CrTFluidSlurryToSlurryRecipe {

        private CrTFluidSlurryToSlurryRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTSlurryStack> getOutputs(FluidSlurryToSlurryRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTSlurryStack::new);
        }
    }
}