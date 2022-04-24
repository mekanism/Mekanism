package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:infuse_type")
@NativeTypeRegistration(value = InfuseType.class, zenCodeName = CrTConstants.CLASS_INFUSE_TYPE)
public class CrTInfuseType {

    private CrTInfuseType() {
    }

    /**
     * Creates a new {@link ICrTInfusionStack} with the given amount of infuse type.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTInfusionStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTInfusionStack makeStack(InfuseType _this, long amount) {
        return new CrTInfusionStack(_this.getStack(amount));
    }

    /**
     * Gets the tags that this infuse type is a part of.
     *
     * @return All the tags this infuse type is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<InfuseType>> getTags(InfuseType _this) {
        return CrTUtils.infuseTypeTags().getTagsFor(_this);
    }
}