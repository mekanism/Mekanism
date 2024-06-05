package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = FluidToFluidRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_FLUID_TO_FLUID)
public class CrTFluidToFluidRecipe {

    private CrTFluidToFluidRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static CTFluidIngredient getInput(FluidToFluidRecipe _this) {
        return CrTUtils.toCrT(_this.getInput());
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<IFluidStack> getOutputs(FluidToFluidRecipe _this) {
        return CrTUtils.convertFluids(_this.getOutputDefinition());
    }
}