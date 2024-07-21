package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import java.util.stream.LongStream;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
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
    public static IIngredientWithAmount getInput(ItemStackToEnergyRecipe _this) {
        return CrTUtils.toCrT(_this.getInput());
    }


    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<Long> getOutput(ItemStackToEnergyRecipe _this) {
        return LongStream.of(_this.getOutputDefinition()).boxed().toList();
    }
}