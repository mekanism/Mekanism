package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_INFUSION_STACK_INGREDIENT)
public class CrTInfusionStackIngredient extends CrTChemicalStackIngredient<InfuseType, InfusionStack, InfusionStackIngredient> {

    /**
     * Creates a {@link CrTInfusionStackIngredient} that matches a given infuse type and amount.
     *
     * @param instance Infuse type to match
     * @param amount   Amount needed
     *
     * @return A {@link CrTInfusionStackIngredient} that matches a given infuse type and amount.
     */
    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(ICrTInfuseType instance, long amount) {
        assertValid(instance, amount, "InfusionStackIngredients", "infuse type");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(instance, amount));
    }

    /**
     * Creates a {@link CrTInfusionStackIngredient} that matches a given infusion stack.
     *
     * @param instance Infusion stack to match
     *
     * @return A {@link CrTInfusionStackIngredient} that matches a given infusion stack.
     */
    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(ICrTInfusionStack instance) {
        assertValid(instance, "InfusionStackIngredients");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(instance.getInternal()));
    }

    /**
     * Creates a {@link CrTInfusionStackIngredient} that matches a given infuse type tag with a given amount.
     *
     * @param infuseTypeTag Tag to match
     * @param amount        Amount needed
     *
     * @return A {@link CrTInfusionStackIngredient} that matches a given infuse type tag with a given amount.
     */
    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(MCTag<ICrTInfuseType> infuseTypeTag, long amount) {
        ITag<InfuseType> tag = assertValidAndGet(infuseTypeTag, amount, CrTInfuseTypeTagManager.INSTANCE::getInternal, "InfusionStackIngredients");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(tag, amount));
    }

    /**
     * Combines multiple {@link CrTInfusionStackIngredient}s into a single {@link CrTInfusionStackIngredient}.
     *
     * @param crtIngredients Ingredients to combine
     *
     * @return A single {@link CrTInfusionStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.Method
    public static CrTInfusionStackIngredient createMulti(CrTInfusionStackIngredient... crtIngredients) {
        return createMulti("InfusionStackIngredients", InfusionStackIngredient[]::new,
              ingredients -> new CrTInfusionStackIngredient(InfusionStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTInfusionStackIngredient(InfusionStackIngredient ingredient) {
        super(ingredient);
    }
}