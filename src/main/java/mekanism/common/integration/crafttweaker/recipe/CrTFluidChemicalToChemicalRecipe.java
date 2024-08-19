package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = FluidChemicalToChemicalRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_FLUID_CHEMICAL_TO_CHEMICAL)
public class CrTFluidChemicalToChemicalRecipe {

    private CrTFluidChemicalToChemicalRecipe() {
    }

    /**
     * Gets the input fluid ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("fluidInput")
    public static CTFluidIngredient getFluidInput(FluidChemicalToChemicalRecipe _this) {
        return CrTUtils.toCrT(_this.getFluidInput());
    }

    /**
     * Gets the input chemical ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("chemicalInput")
    public static ChemicalStackIngredient getChemicalInput(FluidChemicalToChemicalRecipe _this) {
        return _this.getChemicalInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<ICrTChemicalStack> getOutputs(FluidChemicalToChemicalRecipe _this) {
        return CrTUtils.convertChemical(_this.getOutputDefinition());
    }
}