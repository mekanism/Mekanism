package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = PigmentStackIngredient.class, zenCodeName = CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT)
public class CrTPigmentStackIngredient {

    /**
     * Creates a {@link PigmentStackIngredient} that matches a given pigment and amount.
     *
     * @param instance Pigment to match
     * @param amount   Amount needed
     *
     * @return A {@link PigmentStackIngredient} that matches a given pigment and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static PigmentStackIngredient from(ICrTPigment instance, long amount) {
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
        return PigmentStackIngredient.from(instance.getInternal());
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
    public static PigmentStackIngredient from(MCTag<ICrTPigment> pigmentTag, long amount) {
        ITag<Pigment> tag = CrTIngredientHelper.assertValidAndGet(pigmentTag, amount, CrTPigmentTagManager.INSTANCE::getInternal, "PigmentStackIngredients");
        return PigmentStackIngredient.from(tag, amount);
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
}