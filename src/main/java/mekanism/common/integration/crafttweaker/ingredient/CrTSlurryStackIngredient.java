package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTTags;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_SLURRY_STACK_INGREDIENT)
public class CrTSlurryStackIngredient extends CrTChemicalStackIngredient<Slurry, SlurryStack, SlurryStackIngredient> {

    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(ICrTSlurry instance, long amount) {
        assertValid(instance, amount, "SlurryStackIngredients", "slurry");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(instance, amount));
    }

    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(ICrTSlurryStack instance) {
        assertValid(instance, "SlurryStackIngredients");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(instance.getInternal()));
    }

    @ZenCodeType.Method
    public static CrTSlurryStackIngredient from(MCTag slurryTag, long amount) {
        assertValid(slurryTag, amount, CrTTags::isSlurryTag, "SlurryStackIngredients", "SlurryTag");
        return new CrTSlurryStackIngredient(SlurryStackIngredient.from(CrTTags.getSlurryTag(slurryTag), amount));
    }

    @ZenCodeType.Method
    public static CrTSlurryStackIngredient createMulti(CrTSlurryStackIngredient... crtIngredients) {
        return createMulti(SlurryStackIngredient[]::new, ingredients -> new CrTSlurryStackIngredient(SlurryStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTSlurryStackIngredient(SlurryStackIngredient ingredient) {
        super(ingredient);
    }
}