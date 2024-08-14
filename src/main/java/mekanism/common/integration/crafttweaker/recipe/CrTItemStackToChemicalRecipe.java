package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getInput", parameters = {}, getterName = "input")
@NativeTypeRegistration(value = ItemStackToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_CHEMICAL)
public class CrTItemStackToChemicalRecipe {

    private CrTItemStackToChemicalRecipe() {
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<ICrTChemicalStack> getOutputs(ItemStackToChemicalRecipe _this) {
        return CrTUtils.convertChemical(_this.getOutputDefinition());
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
        public static List<ICrTChemicalStack> getOutputs(ItemStackToPigmentRecipe _this) {
            return CrTUtils.convertChemical(_this.getOutputDefinition());
        }
    }
}