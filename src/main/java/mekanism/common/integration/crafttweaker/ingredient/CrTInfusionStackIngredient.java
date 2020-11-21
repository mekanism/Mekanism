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

    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(ICrTInfuseType instance, long amount) {
        assertValid(instance, amount, "InfusionStackIngredients", "infuse type");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(instance, amount));
    }

    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(ICrTInfusionStack instance) {
        assertValid(instance, "InfusionStackIngredients");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(instance.getInternal()));
    }

    @ZenCodeType.Method
    public static CrTInfusionStackIngredient from(MCTag<ICrTInfuseType> infuseTypeTag, long amount) {
        ITag<InfuseType> tag = assertValidAndGet(infuseTypeTag, amount, CrTInfuseTypeTagManager.INSTANCE::getInternal, "InfusionStackIngredients");
        return new CrTInfusionStackIngredient(InfusionStackIngredient.from(tag, amount));
    }

    @ZenCodeType.Method
    public static CrTInfusionStackIngredient createMulti(CrTInfusionStackIngredient... crtIngredients) {
        return createMulti("InfusionStackIngredients", InfusionStackIngredient[]::new,
              ingredients -> new CrTInfusionStackIngredient(InfusionStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTInfusionStackIngredient(InfusionStackIngredient ingredient) {
        super(ingredient);
    }
}