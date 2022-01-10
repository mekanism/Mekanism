package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getInput", parameters = {}, getterName = "input")
@NativeTypeRegistration(value = ItemStackToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL)
public class CrTItemStackToChemicalRecipe {

    private CrTItemStackToChemicalRecipe() {
    }

    @ZenRegister
    @NativeTypeRegistration(value = ItemStackToGasRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_GAS)
    public static class CrTItemStackToGasRecipe {

        private CrTItemStackToGasRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTGasStack> getOutputs(ItemStackToGasRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTGasStack::new);
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ItemStackToInfuseTypeRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_INFUSE_TYPE)
    public static class CrTItemStackToInfuseTypeRecipe {

        private CrTItemStackToInfuseTypeRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTInfusionStack> getOutputs(ItemStackToInfuseTypeRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTInfusionStack::new);
        }
    }

    @ZenRegister
    @NativeTypeRegistration(value = ItemStackToPigmentRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_PIGMENT)
    public static class CrTItemStackToPigmentRecipe {

        private CrTItemStackToPigmentRecipe() {
        }

        /**
         * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("outputs")
        public static List<ICrTPigmentStack> getOutputs(ItemStackToPigmentRecipe _this) {
            return CrTUtils.convert(_this.getOutputDefinition(), CrTPigmentStack::new);
        }
    }
}