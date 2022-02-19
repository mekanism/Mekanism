package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.JSONConverter;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = InfusionStackIngredient.class, zenCodeName = CrTConstants.CLASS_INFUSION_STACK_INGREDIENT)
public class CrTInfusionStackIngredient {

    private CrTInfusionStackIngredient() {
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type and amount.
     *
     * @param instance Infuse type to match
     * @param amount   Amount needed
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(InfuseType instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "InfusionStackIngredients", "infuse type");
        return InfusionStackIngredient.from(instance, amount);
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infusion stack.
     *
     * @param instance Infusion stack to match
     *
     * @return A {@link InfusionStackIngredient} that matches a given infusion stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(ICrTInfusionStack instance) {
        CrTIngredientHelper.assertValid(instance, "InfusionStackIngredients");
        return InfusionStackIngredient.from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type tag with a given amount.
     *
     * @param infuseTypeTag Tag to match
     * @param amount        Amount needed
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(MCTag<InfuseType> infuseTypeTag, long amount) {
        ITag<InfuseType> tag = CrTIngredientHelper.assertValidAndGet(infuseTypeTag, amount, CrTInfuseTypeTagManager.INSTANCE::getInternal, "InfusionStackIngredients");
        return InfusionStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type tag with amount.
     *
     * @param infuseTypeTag Tag and amount to match
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(MCTagWithAmount<InfuseType> infuseTypeTag) {
        return from(infuseTypeTag.getTag(), infuseTypeTag.getAmount());
    }

    /**
     * Combines multiple {@link InfusionStackIngredient}s into a single {@link InfusionStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link InfusionStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient createMulti(InfusionStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("InfusionStackIngredients", InfusionStackIngredient::createMulti, ingredients);
    }

    /**
     * Converts this {@link InfusionStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link InfusionStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(InfusionStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * OR's this {@link InfusionStackIngredient} with another {@link InfusionStackIngredient} to create a multi {@link InfusionStackIngredient}
     *
     * @param other {@link InfusionStackIngredient} to combine with.
     *
     * @return Multi {@link InfusionStackIngredient} that matches both the source {@link InfusionStackIngredient} and the OR'd {@link InfusionStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static InfusionStackIngredient or(InfusionStackIngredient _this, InfusionStackIngredient other) {
        return InfusionStackIngredient.createMulti(_this, other);
    }
}