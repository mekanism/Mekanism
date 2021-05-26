package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.tag.CrTGasTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = GasStackIngredient.class, zenCodeName = CrTConstants.CLASS_GAS_STACK_INGREDIENT)
public class CrTGasStackIngredient {

    private CrTGasStackIngredient() {
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas and amount.
     *
     * @param instance Gas to match
     * @param amount   Amount needed
     *
     * @return A {@link GasStackIngredient} that matches a given gas and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(Gas instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "GasStackIngredients", "gas");
        return GasStackIngredient.from(instance, amount);
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas stack.
     *
     * @param instance Gas stack to match
     *
     * @return A {@link GasStackIngredient} that matches a given gas stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(ICrTGasStack instance) {
        CrTIngredientHelper.assertValid(instance, "GasStackIngredients");
        return GasStackIngredient.from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas tag with a given amount.
     *
     * @param gasTag Tag to match
     * @param amount Amount needed
     *
     * @return A {@link GasStackIngredient} that matches a given gas tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(MCTag<Gas> gasTag, long amount) {
        ITag<Gas> tag = CrTIngredientHelper.assertValidAndGet(gasTag, amount, CrTGasTagManager.INSTANCE::getInternal, "GasStackIngredients");
        return GasStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link GasStackIngredient} that matches a given gas tag with amount.
     *
     * @param gasTag Tag and amount to match
     *
     * @return A {@link GasStackIngredient} that matches a given gas tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient from(MCTagWithAmount<Gas> gasTag) {
        return from(gasTag.getTag(), gasTag.getAmount());
    }

    /**
     * Combines multiple {@link GasStackIngredient}s into a single {@link GasStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link GasStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static GasStackIngredient createMulti(GasStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("GasStackIngredients", GasStackIngredient::createMulti, ingredients);
    }
}