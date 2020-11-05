package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.util.MCResourceLocation;
import java.util.Set;
import java.util.stream.Collectors;
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
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.bracket.IBracketSupport;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL)
public interface ICrTChemical<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      extends CommandStringDisplayable, IBracketSupport, IChemicalProvider<CHEMICAL> {

    /**
     * Wwhether or not this chemical represents the empty type.
     *
     * @return {@code true} if this chemical represents the empty type, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("empty")
    default boolean isEmptyType() {
        return getChemical().isEmptyType();
    }

    /**
     * Whether or not this chemical is hidden from JEI.
     *
     * @return {@code true} if this chemical is hidden, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("hidden")
    default boolean isHidden() {
        return getChemical().isHidden();
    }

    /**
     * Checks if this chemical is in a given tag.
     *
     * @param tag The tag to check.
     *
     * @return {@code true} if the chemical is in the tag, {@code false} otherwise.
     */
    @ZenCodeType.Method
    boolean isIn(MCTag tag);

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    @ZenCodeType.Method
    default Set<MCResourceLocation> getTags() {
        return getChemical().getTags().stream().map(MCResourceLocation::new).collect(Collectors.toSet());
    }

    /**
     * Creates a new {@link ICrTChemical} with the given amount of chemical.
     *
     * @param amount The size of the stack to create
     *
     * @return a new (immutable) {@link ICrTChemical}
     */
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    default CRT_STACK multiply(long amount) {
        return makeStack(amount);
    }

    /**
     * Creates a new {@link ICrTChemical} with the given amount of chemical.
     *
     * @param amount The size of the stack to create
     *
     * @return a new (immutable) {@link ICrTChemical}
     */
    @ZenCodeType.Method
    CRT_STACK makeStack(long amount);

    @Override
    default String getCommandString() {
        return "<" + getBracketName() + ":" + getRegistryName() + ">.type";
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_GAS)
    interface ICrTGas extends ICrTChemical<Gas, GasStack, ICrTGas, ICrTGasStack>, IGasBracketSupport, IGasProvider {

        @Override
        default ICrTGasStack makeStack(long amount) {
            return new CrTGasStack(getStack(amount));
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_INFUSE_TYPE)
    interface ICrTInfuseType extends ICrTChemical<InfuseType, InfusionStack, ICrTInfuseType, ICrTInfusionStack>, IInfuseTypeBracketSupport, IInfuseTypeProvider {

        @Override
        default ICrTInfusionStack makeStack(long amount) {
            return new CrTInfusionStack(getStack(amount));
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_PIGMENT)
    interface ICrTPigment extends ICrTChemical<Pigment, PigmentStack, ICrTPigment, ICrTPigmentStack>, IPigmentBracketSupport, IPigmentProvider {

        @Override
        default ICrTPigmentStack makeStack(long amount) {
            return new CrTPigmentStack(getStack(amount));
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_SLURRY)
    interface ICrTSlurry extends ICrTChemical<Slurry, SlurryStack, ICrTSlurry, ICrTSlurryStack>, ISlurryBracketSupport, ISlurryProvider {

        @Override
        default ICrTSlurryStack makeStack(long amount) {
            return new CrTSlurryStack(getStack(amount));
        }
    }
}