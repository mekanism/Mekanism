package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ItemStackToEnergyRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ENERGY)
public class CrTItemStackToEnergyRecipe {

    private CrTItemStackToEnergyRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static ItemStackIngredient getInput(ItemStackToEnergyRecipe _this) {
        return _this.getInput();
    }


    /**
     * Energy output.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("output")
    public static FloatingLong getOutput(ItemStackToEnergyRecipe _this) {
        //TODO - 1.18: Outputs after we figure out how we are adjusting them
        return _this.getOutputDefinition().copyAsConst();
    }
}