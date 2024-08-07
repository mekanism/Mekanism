package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod.MethodParameter;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeMethod(name = "isEmptyType", parameters = {}, getterName = "empty")
@NativeMethod(name = "getTint", parameters = {}, getterName = "tint")
@NativeMethod(name = "getColorRepresentation", parameters = {}, getterName = "colorRepresentation")
@NativeMethod(name = "getAttributes", parameters = {}, getterName = "attributes")
@NativeMethod(name = "addAttribute", parameters = @MethodParameter(type = ChemicalAttribute.class, name = "attribute"))
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
@TaggableElement("mekanism:chemical")
public class CrTChemical {

    private CrTChemical() {
    }

    /**
     * Creates a new {@link ICrTChemicalStack} with the given amount of infuse type.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTChemicalStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTChemicalStack makeStack(Chemical _this, long amount) {
        return new CrTChemicalStack(_this.getStack(amount));
    }

    /**
     * Gets the tags that this infuse type is a part of.
     *
     * @return All the tags this infuse type is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Chemical>> getTags(Chemical _this) {
        return CrTUtils.chemicalTags().getTagsFor(_this);
    }
}