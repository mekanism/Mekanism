package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
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
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.bracket.IBracketSupport;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_STACK)
public interface ICrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> extends CommandStringDisplayable, IBracketSupport {

    /**
     * Gets the registry name for the chemical this stack is representing.
     *
     * @return A MCResourceLocation representing the registry name.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    default ResourceLocation getRegistryName() {
        return getInternal().getTypeRegistryName();
    }

    /**
     * Whether this chemical stack is empty.
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
     * @return A new stack, or this stack, depending on if this stack is mutable
     */
    @ZenCodeType.Method
    CRT_STACK setAmount(long amount);

    /**
     * Multiplies the stack's amount by the given amount in MilliBuckets (MB)
     *
     * @param amount The amount to multiply the stack by.
     *
     * @return A new stack, or this stack, depending on if this stack is mutable
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
     * @return A new stack, or this stack, depending on if this stack is mutable
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
     * @return A new stack, or this stack, depending on if this stack is mutable
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
    CRT_STACK asMutable();

    /**
     * Makes this stack immutable
     *
     * @return An immutable Stack. This is either a new stack if the current stack is mutable, or the same stack if it is already immutable.
     */
    @ZenCodeType.Method
    CRT_STACK asImmutable();

    /**
     * Copies the stack. Only needed when mutable stacks are involved.
     *
     * @return A new stack, that contains the same info as this one
     */
    @ZenCodeType.Method
    CRT_STACK copy();

    /**
     * Retrieves this chemical stack's chemical.
     *
     * @return The chemical.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("type")
    @ZenCodeType.Caster(implicit = true)
    default CHEMICAL getType() {
        return getInternal().getType();
    }

    /**
     * Mod devs should use this to get the actual ChemicalStack.
     *
     * @return The actual ChemicalStack.
     */
    STACK getInternal();

    /**
     * Mod devs should use this to get the actual ChemicalStack.
     *
     * @return The actual ChemicalStack.
     */
    default STACK getImmutableInternal() {
        return copy().getInternal();
    }

    /**
     * Whether this ChemicalStack's chemical type is equal to the other defined ChemicalStack.
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
     * Casts this chemical stack to a generic {@link ChemicalStackIngredient} for use in recipes that support any chemical type as an input.
     *
     * @apiNote We declare this as generic so that ZenCode can properly match this to the places where we declare all the subtypes as generic.
     */
    @ZenCodeType.Caster(implicit = true)
    ChemicalStackIngredient<?, ?> asChemicalStackIngredient();

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_GAS_STACK)
    interface ICrTGasStack extends ICrTChemicalStack<Gas, GasStack, ICrTGasStack>, IGasBracketSupport {

        @Override
        default ChemicalStackIngredient<Gas, GasStack> asChemicalStackIngredient() {
            return asGasStackIngredient();
        }

        /**
         * Casts this gas stack to a {@link GasStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default GasStackIngredient asGasStackIngredient() {
            return CrTGasStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_INFUSION_STACK)
    interface ICrTInfusionStack extends ICrTChemicalStack<InfuseType, InfusionStack, ICrTInfusionStack>, IInfuseTypeBracketSupport {

        @Override
        default ChemicalStackIngredient<InfuseType, InfusionStack> asChemicalStackIngredient() {
            return asInfusionStackIngredient();
        }

        /**
         * Casts this infusion stack to a {@link InfusionStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default InfusionStackIngredient asInfusionStackIngredient() {
            return CrTInfusionStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_STACK)
    interface ICrTPigmentStack extends ICrTChemicalStack<Pigment, PigmentStack, ICrTPigmentStack>, IPigmentBracketSupport {

        @Override
        default ChemicalStackIngredient<Pigment, PigmentStack> asChemicalStackIngredient() {
            return asPigmentStackIngredient();
        }

        /**
         * Casts this pigment stack to a {@link PigmentStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default PigmentStackIngredient asPigmentStackIngredient() {
            return CrTPigmentStackIngredient.from(this);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_SLURRY_STACK)
    interface ICrTSlurryStack extends ICrTChemicalStack<Slurry, SlurryStack, ICrTSlurryStack>, ISlurryBracketSupport {

        @Override
        default ChemicalStackIngredient<Slurry, SlurryStack> asChemicalStackIngredient() {
            return asSlurryStackIngredient();
        }

        /**
         * Casts this slurry stack to a {@link SlurryStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        default SlurryStackIngredient asSlurryStackIngredient() {
            return CrTSlurryStackIngredient.from(this);
        }
    }
}