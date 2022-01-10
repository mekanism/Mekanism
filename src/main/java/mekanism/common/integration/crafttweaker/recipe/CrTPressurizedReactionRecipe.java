package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = PressurizedReactionRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_REACTION)
public class CrTPressurizedReactionRecipe {

    private CrTPressurizedReactionRecipe() {
    }

    /**
     * Gets the item input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputSolid")
    public static ItemStackIngredient getInputSolid(PressurizedReactionRecipe _this) {
        return _this.getInputSolid();
    }

    /**
     * Gets the fluid input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputFluid")
    public static FluidStackIngredient getInputFluid(PressurizedReactionRecipe _this) {
        return _this.getInputFluid();
    }

    /**
     * Gets the gas input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputGas")
    public static GasStackIngredient getInputGas(PressurizedReactionRecipe _this) {
        return _this.getInputGas();
    }

    /**
     * Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyRequired")
    public static FloatingLong getEnergyRequired(PressurizedReactionRecipe _this) {
        return _this.getEnergyRequired().copyAsConst();
    }

    /**
     * Gets the base duration in ticks that this recipe takes to complete.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("duration")
    public static int getDuration(PressurizedReactionRecipe _this) {
        return _this.getDuration();
    }

    //TODO - 1.18: Outputs after we figure out how we are adjusting them
}