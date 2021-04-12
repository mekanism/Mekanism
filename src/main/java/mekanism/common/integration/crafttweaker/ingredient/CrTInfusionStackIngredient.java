package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = InfusionStackIngredient.class, zenCodeName = CrTConstants.CLASS_INFUSION_STACK_INGREDIENT)
public class CrTInfusionStackIngredient {

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type and amount.
     *
     * @param instance Infuse type to match
     * @param amount   Amount needed
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(ICrTInfuseType instance, long amount) {
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
    public static InfusionStackIngredient from(MCTag<ICrTInfuseType> infuseTypeTag, long amount) {
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
    public static InfusionStackIngredient from(MCTagWithAmount<ICrTInfuseType> infuseTypeTag) {
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
}