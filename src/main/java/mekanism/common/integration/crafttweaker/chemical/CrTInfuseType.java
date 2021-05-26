package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
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
}