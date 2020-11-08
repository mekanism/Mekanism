package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_STACK_INGREDIENT)
public class CrTChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> extends CrTIngredientWrapper<STACK, INGREDIENT> {

    protected static void assertValid(ICrTChemical<?, ?, ?, ?> instance, long amount, String ingredientType, String chemicalType) {
        assertValidAmount(ingredientType, amount);
        Chemical<?> chemical = instance.getChemical();
        if (chemical.isEmptyType()) {
            throw new IllegalArgumentException(ingredientType + " cannot be created from an empty " + chemicalType + ".");
        }
    }

    protected static void assertValid(ICrTChemicalStack<?, ?, ?, ?> instance, String ingredientType) {
        if (instance.getInternal().isEmpty()) {
            throw new IllegalArgumentException(ingredientType + " cannot be created from an empty stack.");
        }
    }

    protected CrTChemicalStackIngredient(INGREDIENT ingredient) {
        super(ingredient);
    }
}