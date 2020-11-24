package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.tag.CrTGasTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_GAS_STACK_INGREDIENT)
public class CrTGasStackIngredient extends CrTChemicalStackIngredient<Gas, GasStack, GasStackIngredient> {

    /**
     * Creates a {@link CrTGasStackIngredient} that matches a given gas and amount.
     *
     * @param instance Gas to match
     * @param amount   Amount needed
     *
     * @return A {@link CrTGasStackIngredient} that matches a given gas and amount.
     */
    @ZenCodeType.Method
    public static CrTGasStackIngredient from(ICrTGas instance, long amount) {
        assertValid(instance, amount, "GasStackIngredients", "gas");
        return new CrTGasStackIngredient(GasStackIngredient.from(instance, amount));
    }

    /**
     * Creates a {@link CrTGasStackIngredient} that matches a given gas stack.
     *
     * @param instance Gas stack to match
     *
     * @return A {@link CrTGasStackIngredient} that matches a given gas stack.
     */
    @ZenCodeType.Method
    public static CrTGasStackIngredient from(ICrTGasStack instance) {
        assertValid(instance, "GasStackIngredients");
        return new CrTGasStackIngredient(GasStackIngredient.from(instance.getInternal()));
    }

    /**
     * Creates a {@link CrTGasStackIngredient} that matches a given gas tag with a given amount.
     *
     * @param gasTag Tag to match
     * @param amount Amount needed
     *
     * @return A {@link CrTGasStackIngredient} that matches a given gas tag with a given amount.
     */
    @ZenCodeType.Method
    public static CrTGasStackIngredient from(MCTag<ICrTGas> gasTag, long amount) {
        ITag<Gas> tag = assertValidAndGet(gasTag, amount, CrTGasTagManager.INSTANCE::getInternal, "GasStackIngredients");
        return new CrTGasStackIngredient(GasStackIngredient.from(tag, amount));
    }

    /**
     * Combines multiple {@link CrTGasStackIngredient}s into a single {@link CrTGasStackIngredient}.
     *
     * @param crtIngredients Ingredients to combine
     *
     * @return A single {@link CrTGasStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.Method
    public static CrTGasStackIngredient createMulti(CrTGasStackIngredient... crtIngredients) {
        return createMulti("GasStackIngredients", GasStackIngredient[]::new,
              ingredients -> new CrTGasStackIngredient(GasStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTGasStackIngredient(GasStackIngredient ingredient) {
        super(ingredient);
    }
}