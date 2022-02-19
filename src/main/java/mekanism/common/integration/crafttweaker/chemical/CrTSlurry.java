package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = Slurry.class, zenCodeName = CrTConstants.CLASS_SLURRY)
public class CrTSlurry {

    private CrTSlurry() {
    }

    /**
     * Creates a new {@link ICrTSlurryStack} with the given amount of slurry.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTSlurryStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTSlurryStack makeStack(Slurry _this, long amount) {
        return new CrTSlurryStack(_this.getStack(amount));
    }
}