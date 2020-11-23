package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import com.blamejared.crafttweaker.impl.util.MCResourceLocation;
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
import mekanism.common.integration.crafttweaker.ingredient.CrTChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_STACK)
public interface ICrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      extends CommandStringDisplayable, IBracketSupport {

    /**
     * Gets the registry name for the chemical this stack is representing.
     *
     * @return A MCResourceLocation representing the registry name.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    default MCResourceLocation getRegistryName() {
        return new MCResourceLocation(getInternal().getTypeRegistryName());
    }

    /**
     * Whether or not this chemical stack is empty.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("empty")
    default boolean isEmpty() {
        return getInternal().isEmpty();
    }

    /**
     * Gets the size of this chemical stack.
     *
     * @return The size of this chemical stack or zero if it is empty
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("amount")
    default long getAmount() {
        return getInternal().getAmount();
    }

    /**
     * Sets the chemical's amount in MilliBuckets (MB)
     *
     * @param amount The amount to set the stack's amount to.
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     */
    @ZenCodeType.Method
    CRT_STACK setAmount(long amount);

    /**
     * Multiplies the stack's amount by the given amount in MilliBuckets (MB)
     *
     * @param amount The amount to multiply the stack by.
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     *
     * @implNote No checks are made to ensure that the long does not overflow.
     */
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    default CRT_STACK multiply(long amount) {
        return setAmount(getAmount() * amount);
    }

    /**
     * Grows the stack's amount by the given amount in MilliBuckets (MB)
     *
     * @param amount The amount to grow the stack by.
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     *
     * @apiNote Negative values are valid and will instead shrink the stack.
     * @implNote No checks are made to ensure that the long does not overflow.
     */
    @ZenCodeType.Method
    default CRT_STACK grow(long amount) {
        return setAmount(getAmount() + amount);
    }

    /**
     * Shrinks the stack's amount by the given amount in MilliBuckets (MB)
     *
     * @param amount The amount to shrink the stack by.
     *
     * @return A new stack, or this stack, depending if this stack is mutable
     *
     * @apiNote Negative values are valid and will instead grow the stack.
     * @implNote No checks are made to ensure that the long does not underflow.
     */
    @ZenCodeType.Method
    default CRT_STACK shrink(long amount) {
        return setAmount(getAmount() - amount);
    }

    /**
     * Checks if this chemical stack, contains the given chemical stack by checking if the chemicals are the same, and if this stack's amount is bigger than the given
     * stack's amount
     *
     * @param stack Chemical stack to compare against
     *
     * @return {@code true} if this stack contains the other stack
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.CONTAINS)
    default boolean containsOther(CRT_STACK stack) {
        return isTypeEqual(stack) && getInternal().getAmount() >= stack.getInternal().getAmount();
    }

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
    @ZenCodeType.Method
    @ZenCodeType.Getter("type")
    @ZenCodeType.Caster(implicit = true)
    CRT_CHEMICAL getType();

    /**
     * Mod devs should use this to get the actual ChemicalStack.
     *
     * @return The actual ChemicalStack.
     */
    STACK getInternal();

    /**
     * Whether or not this ChemicalStack's chemical type is equal to the other defined ChemicalStack.
     *
     * @param stack - ChemicalStack to check
     *
     * @return if the ChemicalStacks contain the same chemical type
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.CONTAINS)
    default boolean isTypeEqual(CRT_STACK stack) {
        return getInternal().isTypeEqual(stack.getInternal());
    }

    /**
     * Checks if this chemical stack is equal another chemical stack.
     *
     * @param other Chemical stack to check against.
     *
     * @return {@code true} if the chemicals stacks are equal, {@code false} otherwise.
     *
     * @implNote This mimics how CraftTweaker handles mutable vs immutable stacks in that even if they represent the same internal stack, they will return false for if
     * they are equal if one is mutable and one is immutable.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
    default boolean isEqual(CRT_STACK other) {
        return equals(other);
    }

    /**
     * Casts this chemical stack to a generic {@link CrTChemicalStackIngredient} for use in recipes that support any chemical type as an input.
     *
     * @apiNote We declare this as generic so that ZenCode can properly match this to the places where we declare all the sub types as generic.
     */
    @ZenCodeType.Caster(implicit = true)
    CrTChemicalStackIngredient<?, ?, ?> asChemicalStackIngredient();

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_GAS_STACK)
    interface ICrTGasStack extends ICrTChemicalStack<Gas, GasStack, ICrTGas, ICrTGasStack>, IGasBracketSupport {

        @Override
        default ICrTGas getType() {
            return new CrTGas(getInternal().getType());
        }

        @Override
        default CrTChemicalStackIngredient<Gas, GasStack, ?> asChemicalStackIngredient() {
            return asGasStackIngredient();
        }

        /**
         * Casts this gas stack to a {@link CrTGasStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default CrTGasStackIngredient asGasStackIngredient() {
            return CrTGasStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_INFUSION_STACK)
    interface ICrTInfusionStack extends ICrTChemicalStack<InfuseType, InfusionStack, ICrTInfuseType, ICrTInfusionStack>, IInfuseTypeBracketSupport {

        @Override
        default ICrTInfuseType getType() {
            return new CrTInfuseType(getInternal().getType());
        }

        @Override
        default CrTChemicalStackIngredient<InfuseType, InfusionStack, ?> asChemicalStackIngredient() {
            return asInfusionStackIngredient();
        }

        /**
         * Casts this infusion stack to a {@link CrTInfusionStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default CrTInfusionStackIngredient asInfusionStackIngredient() {
            return CrTInfusionStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_STACK)
    interface ICrTPigmentStack extends ICrTChemicalStack<Pigment, PigmentStack, ICrTPigment, ICrTPigmentStack>, IPigmentBracketSupport {

        @Override
        default ICrTPigment getType() {
            return new CrTPigment(getInternal().getType());
        }

        @Override
        default CrTChemicalStackIngredient<Pigment, PigmentStack, ?> asChemicalStackIngredient() {
            return asPigmentStackIngredient();
        }

        /**
         * Casts this pigment stack to a {@link CrTPigmentStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default CrTPigmentStackIngredient asPigmentStackIngredient() {
            return CrTPigmentStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_SLURRY_STACK)
    interface ICrTSlurryStack extends ICrTChemicalStack<Slurry, SlurryStack, ICrTSlurry, ICrTSlurryStack>, ISlurryBracketSupport {

        @Override
        default ICrTSlurry getType() {
            return new CrTSlurry(getInternal().getType());
        }

        @Override
        default CrTChemicalStackIngredient<Slurry, SlurryStack, ?> asChemicalStackIngredient() {
            return asSlurryStackIngredient();
        }

        /**
         * Casts this slurry stack to a {@link CrTSlurryStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default CrTSlurryStackIngredient asSlurryStackIngredient() {
            return CrTSlurryStackIngredient.from(this);
        }
    }
}