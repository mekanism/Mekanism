package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
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
}