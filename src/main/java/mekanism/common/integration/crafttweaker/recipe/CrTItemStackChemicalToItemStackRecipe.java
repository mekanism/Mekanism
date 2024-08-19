package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "getItemInput", parameters = {}, getterName = "itemInput")
@NativeMethod(name = "getChemicalInput", parameters = {}, getterName = "chemicalInput")
@NativeTypeRegistration(value = ItemStackChemicalToItemStackRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_CHEMICAL_TO_ITEM_STACK)
public class CrTItemStackChemicalToItemStackRecipe {

    private CrTItemStackChemicalToItemStackRecipe() {
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<IItemStack> getOutputs(ItemStackChemicalToItemStackRecipe _this) {
        return CrTUtils.convertItems(_this.getOutputDefinition());
    }

    @ZenRegister
    @NativeTypeRegistration(value = NucleosynthesizingRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_NUCLEOSYNTHESIZING)
    public static class CrTNucleosynthesizingRecipe {

        private CrTNucleosynthesizingRecipe() {
        }

        /**
         * Gets the duration in ticks this recipe takes to complete.
         */
        @ZenCodeType.Method
        @ZenCodeType.Getter("duration")
        public static int getDuration(NucleosynthesizingRecipe _this) {
            return _this.getDuration();
        }
    }
}