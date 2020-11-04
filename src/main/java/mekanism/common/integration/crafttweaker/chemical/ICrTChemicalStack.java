package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.bracket.IBracketSupport;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTGas;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTPigment;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_STACK)
public interface ICrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      extends CommandStringDisplayable, IBracketSupport {

    /**
     * Sets the fluid amount in MilliBuckets (MB)
     *
     * @param amount The amount to multiply this stack
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     *
     * @docParam amount 1000
     */
    @ZenCodeType.Method
    CRT_STACK setAmount(long amount);//TODO: Do we want to add other helpers like grow, shrink etc


    /**
     * Sets the fluid amount in MilliBuckets (MB)
     *
     * @param amount The amount to multiply this stack
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     *
     * @docParam amount 1000
     */
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    CRT_STACK multiply(long amount);

    /**
     * Makes this stack mutable
     *
     * @return A new Stack, that is mutable.
     */
    @ZenCodeType.Method
    CRT_STACK mutable();

    /**
     * Copies the stack. Only needed when mutable stacks are involved.
     *
     * @return A new stack, that contains the same info as this one
     */
    @ZenCodeType.Method
    CRT_STACK copy();

    /**
     * Retrieves this fluid stack's fluid.
     *
     * @return The fluid.
     */
    @ZenCodeType.Getter("type")//TODO: Can we have this be .type or .chemical?
    CRT_CHEMICAL getType();

    /**
     * Moddevs, use this to get the Vanilla version.
     */
    STACK getInternal();

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_GAS_STACK)
    interface ICrTGasStack extends ICrTChemicalStack<Gas, GasStack, ICrTGas, ICrTGasStack>, IGasBracketSupport {

        @Override
        default ICrTGas getType() {
            return new CrTGas(getInternal().getType());
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_INFUSION_STACK)
    interface ICrTInfusionStack extends ICrTChemicalStack<InfuseType, InfusionStack, ICrTInfuseType, ICrTInfusionStack>, IInfuseTypeBracketSupport {

        @Override
        default ICrTInfuseType getType() {
            return new CrTInfuseType(getInternal().getType());
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_STACK)
    interface ICrTPigmentStack extends ICrTChemicalStack<Pigment, PigmentStack, ICrTPigment, ICrTPigmentStack>, IPigmentBracketSupport {

        @Override
        default ICrTPigment getType() {
            return new CrTPigment(getInternal().getType());
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_SLURRY_STACK)
    interface ICrTSlurryStack extends ICrTChemicalStack<Slurry, SlurryStack, ICrTSlurry, ICrTSlurryStack>, ISlurryBracketSupport {

        @Override
        default ICrTSlurry getType() {
            return new CrTSlurry(getInternal().getType());
        }
    }
}