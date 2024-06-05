package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = CombinerRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_COMBINING)
public class CrTCombinerRecipe {

    private CrTCombinerRecipe() {
    }

    /**
     * Gets the main input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("mainInput")
    public static IIngredientWithAmount getMainInput(CombinerRecipe _this) {
        return CrTUtils.toCrT(_this.getMainInput());
    }

    /**
     * Gets the secondary input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("extraInput")
    public static IIngredientWithAmount getExtraInput(CombinerRecipe _this) {
        return CrTUtils.toCrT(_this.getExtraInput());
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<IItemStack> getOutputs(CombinerRecipe _this) {
        return CrTUtils.convertItems(_this.getOutputDefinition());
    }
}