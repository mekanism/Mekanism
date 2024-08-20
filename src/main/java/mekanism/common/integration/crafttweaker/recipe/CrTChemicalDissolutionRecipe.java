package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalDissolutionRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class CrTChemicalDissolutionRecipe {

    private CrTChemicalDissolutionRecipe() {
    }

    /**
     * Represents whether this recipe consumes the chemical each tick.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("perTickUsage")
    public static boolean isPerTickUsage(ChemicalDissolutionRecipe _this) {
        return _this.perTickUsage();
    }

    /**
     * Gets the input item ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("itemInput")
    public static IIngredientWithAmount getItemInput(ChemicalDissolutionRecipe _this) {
        return CrTUtils.toCrT(_this.getItemInput());
    }

    /**
     * Gets the input chemical ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("chemicalInput")
    public static ChemicalStackIngredient getChemicalInput(ChemicalDissolutionRecipe _this) {
        return _this.getChemicalInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<ICrTChemicalStack> getOutputs(ChemicalDissolutionRecipe _this) {
        return _this.getOutputDefinition().stream()
              .<ICrTChemicalStack>map(CrTChemicalStack::new)
              .toList();
    }
}