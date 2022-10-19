package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = RotaryRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ROTARY)
public class CrTRotaryRecipe {

    private CrTRotaryRecipe() {
    }

    /**
     * Gets the gas to fluid conversion this rotary recipe has or {@code null} if it only has a fluid to gas conversion.
     */
    @ZenCodeType.Nullable
    @ZenCodeType.Method
    @ZenCodeType.Getter("gasToFluid")
    public static GasToFluid getGasToFluid(RotaryRecipe _this) {
        if (_this.hasGasToFluid()) {
            return new GasToFluid(_this.getGasInput(), CrTUtils.convertFluids(_this.getFluidOutputDefinition()));
        }
        return null;
    }

    /**
     * Gets the fluid to gas conversion this rotary recipe has or {@code null} if it only has a gas to fluid conversion.
     */
    @ZenCodeType.Nullable
    @ZenCodeType.Method
    @ZenCodeType.Getter("fluidToGas")
    public static FluidToGas getFluidToGas(RotaryRecipe _this) {
        if (_this.hasFluidToGas()) {
            return new FluidToGas(_this.getFluidInput(), CrTUtils.convertGas(_this.getGasOutputDefinition()));
        }
        return null;
    }

    /**
     * @param input   Input ingredient.
     * @param outputs Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY_GAS_TO_FLUID)
    public record GasToFluid(@ZenCodeType.Getter("input") GasStackIngredient input,
                             @ZenCodeType.Getter("outputs") List<IFluidStack> outputs) {
    }

    /**
     * @param input   Input ingredient.
     * @param outputs Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY_FLUID_TO_GAS)
    public record FluidToGas(@ZenCodeType.Getter("input") FluidStackIngredient input,
                             @ZenCodeType.Getter("outputs") List<ICrTGasStack> outputs) {
    }
}