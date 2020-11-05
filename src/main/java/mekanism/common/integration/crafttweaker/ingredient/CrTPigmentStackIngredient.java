package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTTags;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT)
public class CrTPigmentStackIngredient extends CrTChemicalStackIngredient<Pigment, PigmentStack, PigmentStackIngredient> {

    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(ICrTPigment instance, long amount) {
        assertValid(instance, amount, "PigmentStackIngredients", "pigment");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(instance, amount));
    }

    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(ICrTPigmentStack instance) {
        assertValid(instance, "PigmentStackIngredients");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(instance.getInternal()));
    }

    @ZenCodeType.Method
    public static CrTPigmentStackIngredient from(MCTag pigmentTag, long amount) {
        assertValid(pigmentTag, amount, CrTTags::isPigmentTag, "PigmentStackIngredients", "PigmentTag");
        return new CrTPigmentStackIngredient(PigmentStackIngredient.from(CrTTags.getPigmentTag(pigmentTag), amount));
    }

    @ZenCodeType.Method
    public static CrTPigmentStackIngredient createMulti(CrTPigmentStackIngredient... crtIngredients) {
        return createMulti(PigmentStackIngredient[]::new, ingredients -> new CrTPigmentStackIngredient(PigmentStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTPigmentStackIngredient(PigmentStackIngredient ingredient) {
        super(ingredient);
    }
}