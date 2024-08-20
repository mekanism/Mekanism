package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = RotaryRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ROTARY)
public class CrTRotaryRecipe {

    private CrTRotaryRecipe() {
    }

    /**
     * Gets the chemical to fluid conversion this rotary recipe has or {@code null} if it only has a fluid to chemical conversion.
     */
    @ZenCodeType.Nullable
    @ZenCodeType.Method
    @ZenCodeType.Getter("chemicalToFluid")
    public static ChemicalToFluid getChemicalToFluid(RotaryRecipe _this) {
        if (_this.hasChemicalToFluid()) {
            return new ChemicalToFluid(_this.getChemicalInput(), CrTUtils.convertFluids(_this.getFluidOutputDefinition()));
        }
        return null;
    }

    /**
     * Gets the fluid to chemical conversion this rotary recipe has or {@code null} if it only has a chemical to fluid conversion.
     */
    @ZenCodeType.Nullable
    @ZenCodeType.Method
    @ZenCodeType.Getter("fluidToChemical")
    public static FluidToChemical getFluidToChemical(RotaryRecipe _this) {
        if (_this.hasFluidToChemical()) {
            return new FluidToChemical(CrTUtils.toCrT(_this.getFluidInput()), CrTUtils.convertChemical(_this.getChemicalOutputDefinition()));
        }
        return null;
    }

    /**
     * @param input   Input ingredient.
     * @param outputs Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY_CHEMICAL_TO_FLUID)
    public record ChemicalToFluid(@ZenCodeType.Getter("input") ChemicalStackIngredient input,
                                  @ZenCodeType.Getter("outputs") List<IFluidStack> outputs) {
    }

    /**
     * @param input   Input ingredient.
     * @param outputs Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_ROTARY_FLUID_TO_CHEMICAL)
    public record FluidToChemical(@ZenCodeType.Getter("input") CTFluidIngredient input,
                                  @ZenCodeType.Getter("outputs") List<ICrTChemicalStack> outputs) {
    }
}