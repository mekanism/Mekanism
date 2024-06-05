package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = SawmillRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_SAWING)
public class CrTSawmillRecipe {

    private CrTSawmillRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static IIngredientWithAmount getInput(SawmillRecipe _this) {
        return CrTUtils.toCrT(_this.getInput());
    }

    /**
     * Main output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("mainOutputs")
    public static List<IItemStack> getMainOutputs(SawmillRecipe _this) {
        return CrTUtils.convertItems(_this.getMainOutputDefinition());
    }

    /**
     * Secondary output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     * Secondary outputs have a chance of {@link #getSecondaryChance(SawmillRecipe)} to be produced.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("secondaryOutputs")
    public static List<IItemStack> getSecondaryOutputs(SawmillRecipe _this) {
        return CrTUtils.convertItems(_this.getSecondaryOutputDefinition());
    }

    /**
     * Gets the chance (between 0 and 1) of the secondary output being produced.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("secondaryChance")
    public static double getSecondaryChance(SawmillRecipe _this) {
        return _this.getSecondaryChance();
    }
}