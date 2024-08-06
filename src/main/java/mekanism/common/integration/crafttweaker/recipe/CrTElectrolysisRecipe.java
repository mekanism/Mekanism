package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ElectrolysisRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_SEPARATING)
public class CrTElectrolysisRecipe {

    private CrTElectrolysisRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static CTFluidIngredient getInput(ElectrolysisRecipe _this) {
        return CrTUtils.toCrT(_this.getInput());
    }

    /**
     * Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyMultiplier")
    public static long getEnergyMultiplier(ElectrolysisRecipe _this) {
        return _this.getEnergyMultiplier();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<CrTElectrolysisRecipeOutput> getOutputs(ElectrolysisRecipe _this) {
        return CrTUtils.convert(_this.getOutputDefinition(), output -> new CrTElectrolysisRecipeOutput(new CrTChemicalStack(output.left()), new CrTChemicalStack(output.right())));
    }

    /**
     * @param left  Left output.
     * @param right Right output.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SEPARATING_OUTPUT)
    public record CrTElectrolysisRecipeOutput(@ZenCodeType.Getter("left") ICrTChemicalStack left,
                                              @ZenCodeType.Getter("right") ICrTChemicalStack right) {
    }
}