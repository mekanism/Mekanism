package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ChemicalDissolutionRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_DISSOLUTION)
public class CrTChemicalDissolutionRecipe {

    private CrTChemicalDissolutionRecipe() {
    }

    /**
     * Gets the input item ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("itemInput")
    public static ItemStackIngredient getItemInput(ChemicalDissolutionRecipe _this) {
        return _this.getItemInput();
    }

    /**
     * Gets the input gas ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("gasInput")
    public static GasStackIngredient getGasInput(ChemicalDissolutionRecipe _this) {
        return _this.getGasInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<ICrTChemicalStack<?, ?, ?>> getOutputs(ChemicalDissolutionRecipe _this) {
        return _this.getOutputDefinition().stream()
              .<ICrTChemicalStack<?, ?, ?>>map(CrTUtils::fromBoxedStack)
              .filter(Objects::nonNull)
              .toList();
    }
}