package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.JSONConverter;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = PigmentStackIngredient.class, zenCodeName = CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT)
public class CrTPigmentStackIngredient {

    private CrTPigmentStackIngredient() {
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment and amount.
     *
     * @param instance Pigment to match
     * @param amount   Amount needed
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(Pigment instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "PigmentStackIngredients", "pigment");
        return PigmentStackIngredient.from(instance, amount);
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment stack.
     *
     * @param instance Pigment stack to match
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(ICrTPigmentStack instance) {
        CrTIngredientHelper.assertValid(instance, "PigmentStackIngredients");
        return PigmentStackIngredient.from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment tag with a given amount.
     *
     * @param pigmentTag Tag to match
     * @param amount     Amount needed
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(MCTag<Pigment> pigmentTag, long amount) {
        ITag<Pigment> tag = CrTIngredientHelper.assertValidAndGet(pigmentTag, amount, CrTPigmentTagManager.INSTANCE::getInternal, "PigmentStackIngredients");
        return PigmentStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment tag with amount.
     *
     * @param pigmentTag Tag and amount to match
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(MCTagWithAmount<Pigment> pigmentTag) {
        return from(pigmentTag.getTag(), pigmentTag.getAmount());
    }

    /**
     * Combines multiple {@link PigmentStackIngredient}s into a single {@link PigmentStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link PigmentStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient createMulti(PigmentStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("PigmentStackIngredients", PigmentStackIngredient::createMulti, ingredients);
    }

    /**
     * Converts this {@link PigmentStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link PigmentStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(PigmentStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * OR's this {@link PigmentStackIngredient} with another {@link PigmentStackIngredient} to create a multi {@link PigmentStackIngredient}
     *
     * @param other {@link PigmentStackIngredient} to combine with.
     *
     * @return Multi {@link PigmentStackIngredient} that matches both the source {@link PigmentStackIngredient} and the OR'd {@link PigmentStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static PigmentStackIngredient or(PigmentStackIngredient _this, PigmentStackIngredient other) {
        return PigmentStackIngredient.createMulti(_this, other);
    }
}