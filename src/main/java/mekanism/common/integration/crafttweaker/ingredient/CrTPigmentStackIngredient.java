package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT)
public class CrTPigmentStackIngredient extends CrTChemicalStackIngredient<Pigment, PigmentStack, PigmentStackIngredient> {

    /**
     * Creates a {@link CrTPigmentStackIngredient} that matches a given pigment and amount.
     *
     * @param instance Pigmnt to match
     * @param amount   Amount needed
     *
     * @return A {@link CrTPigmentStackIngredient} that matches a given pigment and amount.
     */
    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(ICrTPigment instance, long amount) {
        assertValid(instance, amount, "PigmentStackIngredients", "pigment");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(instance, amount));
    }

    /**
     * Creates a {@link CrTPigmentStackIngredient} that matches a given pigment stack.
     *
     * @param instance Pigment stack to match
     *
     * @return A {@link CrTPigmentStackIngredient} that matches a given pigment stack.
     */
    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(ICrTPigmentStack instance) {
        assertValid(instance, "PigmentStackIngredients");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(instance.getInternal()));
    }

    /**
     * Creates a {@link CrTPigmentStackIngredient} that matches a given pigment tag with a given amount.
     *
     * @param pigmentTag Tag to match
     * @param amount     Amount needed
     *
     * @return A {@link CrTPigmentStackIngredient} that matches a given pigment tag with a given amount.
     */
    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(MCTag<ICrTPigment> pigmentTag, long amount) {
        ITag<Pigment> tag = assertValidAndGet(pigmentTag, amount, CrTPigmentTagManager.INSTANCE::getInternal, "PigmentStackIngredients");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(tag, amount));
    }

    /**
     * Combines multiple {@link CrTPigmentStackIngredient}s into a single {@link CrTPigmentStackIngredient}.
     *
     * @param crtIngredients Ingredients to combine
     *
     * @return A single {@link CrTPigmentStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.Method
    public static CrTPigmentStackIngredient createMulti(CrTPigmentStackIngredient... crtIngredients) {
        return createMulti("PigmentStackIngredients", PigmentStackIngredient[]::new,
              ingredients -> new CrTPigmentStackIngredient(PigmentStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTPigmentStackIngredient(PigmentStackIngredient ingredient) {
        super(ingredient);
    }
}