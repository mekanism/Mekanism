package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.tag.CrTSlurryTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = SlurryStackIngredient.class, zenCodeName = CrTConstants.CLASS_SLURRY_STACK_INGREDIENT)
public class CrTSlurryStackIngredient {

    /**
     * Creates a {@link SlurryStackIngredient} that matches a given slurry and amount.
     *
     * @param instance Slurry to match
     * @param amount   Amount needed
     *
     * @return A {@link SlurryStackIngredient} that matches a given slurry and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static SlurryStackIngredient from(ICrTSlurry instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "SlurryStackIngredients", "slurry");
        return SlurryStackIngredient.from(instance, amount);
    }

    /**
     * Creates a {@link SlurryStackIngredient} that matches a given slurry stack.
     *
     * @param instance Slurry stack to match
     *
     * @return A {@link SlurryStackIngredient} that matches a given slurry stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static SlurryStackIngredient from(ICrTSlurryStack instance) {
        CrTIngredientHelper.assertValid(instance, "SlurryStackIngredients");
        return SlurryStackIngredient.from(instance.getInternal());
    }

    /**
     * Creates a {@link SlurryStackIngredient} that matches a given slurry tag with a given amount.
     *
     * @param slurryTag Tag to match
     * @param amount    Amount needed
     *
     * @return A {@link SlurryStackIngredient} that matches a given slurry tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static SlurryStackIngredient from(MCTag<ICrTSlurry> slurryTag, long amount) {
        ITag<Slurry> tag = CrTIngredientHelper.assertValidAndGet(slurryTag, amount, CrTSlurryTagManager.INSTANCE::getInternal, "SlurryStackIngredients");
        return SlurryStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link SlurryStackIngredient} that matches a given slurry tag with amount.
     *
     * @param slurryTag Tag and amount to match
     *
     * @return A {@link SlurryStackIngredient} that matches a given slurry tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static SlurryStackIngredient from(MCTagWithAmount<ICrTSlurry> slurryTag) {
        return from(slurryTag.getTag(), slurryTag.getAmount());
    }

    /**
     * Combines multiple {@link SlurryStackIngredient}s into a single {@link SlurryStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link SlurryStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static SlurryStackIngredient createMulti(SlurryStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("SlurryStackIngredients", SlurryStackIngredient::createMulti, ingredients);
    }
}