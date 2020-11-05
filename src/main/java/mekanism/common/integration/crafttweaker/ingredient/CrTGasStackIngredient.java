package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTTags;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_GAS_STACK_INGREDIENT)
public class CrTGasStackIngredient extends CrTChemicalStackIngredient<Gas, GasStack, GasStackIngredient> {

    @ZenCodeType.Method
    public static CrTGasStackIngredient from(ICrTGas instance, long amount) {
        assertValid(instance, amount, "GasStackIngredients", "gas");
        return new CrTGasStackIngredient(GasStackIngredient.from(instance, amount));
    }

    @ZenCodeType.Method
    public static CrTGasStackIngredient from(ICrTGasStack instance) {
        assertValid(instance, "GasStackIngredients");
        return new CrTGasStackIngredient(GasStackIngredient.from(instance.getInternal()));
    }

    @ZenCodeType.Method
    public static CrTGasStackIngredient from(MCTag gasTag, long amount) {
        assertValid(gasTag, amount, CrTTags::isGasTag, "GasStackIngredients", "GasTag");
        return new CrTGasStackIngredient(GasStackIngredient.from(CrTTags.getGasTag(gasTag), amount));
    }

    @ZenCodeType.Method
    public static CrTGasStackIngredient createMulti(CrTGasStackIngredient... crtIngredients) {
        return createMulti(GasStackIngredient[]::new, ingredients -> new CrTGasStackIngredient(GasStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTGasStackIngredient(GasStackIngredient ingredient) {
        super(ingredient);
    }
}