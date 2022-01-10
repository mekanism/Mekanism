package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
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
    public static FluidStackIngredient getInput(ElectrolysisRecipe _this) {
        return _this.getInput();
    }

    /**
     * Gets the multiplier to the energy cost in relation to the configured hydrogen separating energy cost.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyMultiplier")
    public static FloatingLong getEnergyMultiplier(ElectrolysisRecipe _this) {
        return _this.getEnergyMultiplier().copyAsConst();
    }

    //TODO - 1.18: Outputs after we figure out how we are adjusting them
}