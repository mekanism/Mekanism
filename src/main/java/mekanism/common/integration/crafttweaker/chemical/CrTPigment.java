package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:pigment")
@NativeTypeRegistration(value = Pigment.class, zenCodeName = CrTConstants.CLASS_PIGMENT)
public class CrTPigment {

    private CrTPigment() {
    }

    /**
     * Creates a new {@link ICrTPigmentStack} with the given amount of pigment.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTPigmentStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTPigmentStack makeStack(Pigment _this, long amount) {
        return new CrTPigmentStack(_this.getStack(amount));
    }

    /**
     * Gets the tags that this pigment is a part of.
     *
     * @return All the tags this pigment is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Pigment>> getTags(Pigment _this) {
        return CrTUtils.pigmentTags().getTagsFor(_this);
    }
}