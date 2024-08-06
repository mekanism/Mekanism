package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalCrystallizerRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_CRYSTALLIZING)
public class CrTChemicalCrystallizerRecipe {

    private CrTChemicalCrystallizerRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static ChemicalStackIngredient getInput(ChemicalCrystallizerRecipe _this) {
        return _this.getInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<IItemStack> getOutputs(ChemicalCrystallizerRecipe _this) {
        return CrTUtils.convertItems(_this.getOutputDefinition());
    }
}