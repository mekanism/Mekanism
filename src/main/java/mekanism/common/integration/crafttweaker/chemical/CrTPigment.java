package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
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
}