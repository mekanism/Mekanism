package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.tag.CrTSlurryTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_SLURRY_STACK_INGREDIENT)
public class CrTSlurryStackIngredient extends CrTChemicalStackIngredient<Slurry, SlurryStack, SlurryStackIngredient> {

    /**
     * Creates a {@link CrTSlurryStackIngredient} that matches a given slurry and amount.
     *
     * @param instance Slurry to match
     * @param amount   Amount needed
     *
     * @return A {@link CrTSlurryStackIngredient} that matches a given slurry and amount.
     */
    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(ICrTSlurry instance, long amount) {
        assertValid(instance, amount, "SlurryStackIngredients", "slurry");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(instance, amount));
    }

    /**
     * Creates a {@link CrTSlurryStackIngredient} that matches a given slurry stack.
     *
     * @param instance Slurry stack to match
     *
     * @return A {@link CrTSlurryStackIngredient} that matches a given slurry stack.
     */
    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(ICrTSlurryStack instance) {
        assertValid(instance, "SlurryStackIngredients");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(instance.getInternal()));
    }

    /**
     * Creates a {@link CrTSlurryStackIngredient} that matches a given slurry tag with a given amount.
     *
     * @param slurryTag Tag to match
     * @param amount    Amount needed
     *
     * @return A {@link CrTSlurryStackIngredient} that matches a given slurry tag with a given amount.
     */
    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(MCTag<ICrTSlurry> slurryTag, long amount) {
        ITag<Slurry> tag = assertValidAndGet(slurryTag, amount, CrTSlurryTagManager.INSTANCE::getInternal, "SlurryStackIngredients");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(tag, amount));
    }

    /**
     * Combines multiple {@link CrTSlurryStackIngredient}s into a single {@link CrTSlurryStackIngredient}.
     *
     * @param crtIngredients Ingredients to combine
     *
     * @return A single {@link CrTSlurryStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.Method
    public static CrTSlurryStackIngredient createMulti(CrTSlurryStackIngredient... crtIngredients) {
        return createMulti("SlurryStackIngredients", SlurryStackIngredient[]::new,
              ingredients -> new CrTSlurryStackIngredient(SlurryStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTSlurryStackIngredient(SlurryStackIngredient ingredient) {
        super(ingredient);
    }
}