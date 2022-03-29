package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:gas")
@NativeTypeRegistration(value = Gas.class, zenCodeName = CrTConstants.CLASS_GAS)
public class CrTGas {

    private CrTGas() {
    }

    /**
     * Creates a new {@link ICrTGasStack} with the given amount of gas.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTGasStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTGasStack makeStack(Gas _this, long amount) {
        return new CrTGasStack(_this.getStack(amount));
    }

    /**
     * Gets the tags that this gas is a part of.
     *
     * @return All the tags this gas is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Gas>> getTags(Gas _this) {
        return CrTUtils.gasTags().getTagsFor(_this);
    }
}