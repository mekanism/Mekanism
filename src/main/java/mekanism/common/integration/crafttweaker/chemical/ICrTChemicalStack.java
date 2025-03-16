package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.ingredient.CrTChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_STACK)
public interface ICrTChemicalStack extends CommandStringDisplayable {

    /**
     * Gets the registry name for the chemical this stack is representing.
     *
     * @return A MCResourceLocation representing the registry name.
     */
    @NotNull
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    default ResourceLocation getRegistryName() {
        return CrTChemical.getRegistryName(getChemical());
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
    ICrTChemicalStack setAmount(long amount);

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
    default ICrTChemicalStack multiply(long amount) {
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
    default ICrTChemicalStack grow(long amount) {
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
    default ICrTChemicalStack shrink(long amount) {
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
    default boolean containsOther(ICrTChemicalStack stack) {
        return getAmount() >= stack.getAmount() && isTypeEqual(stack);
    }

    /**
     * Makes this stack mutable
     *
     * @return A new Stack, that is mutable.
     */
    @ZenCodeType.Method
    ICrTChemicalStack asMutable();

    /**
     * Makes this stack immutable
     *
     * @return An immutable Stack. This is either a new stack if the current stack is mutable, or the same stack if it is already immutable.
     */
    @ZenCodeType.Method
    ICrTChemicalStack asImmutable();

    /**
     * Copies the stack. Only needed when mutable stacks are involved.
     *
     * @return A new stack, that contains the same info as this one
     */
    @ZenCodeType.Method
    ICrTChemicalStack copy();

    /**
     * Retrieves this chemical stack's chemical.
     *
     * @return The chemical.
     */
    @ZenCodeType.Method("getType")
    @ZenCodeType.Getter("type")
    @ZenCodeType.Caster(implicit = true)
    default Chemical getChemical() {
        return getInternal().getChemical();
    }

    /**
     * Mod devs should use this to get the actual ChemicalStack.
     *
     * @return The actual ChemicalStack.
     */
    ChemicalStack getInternal();

    /**
     * Mod devs should use this to get the chemical holder.
     *
     * @since 10.7.11
     */
    default Holder<Chemical> getChemicalHolder() {
        return getInternal().getChemicalHolder();
    }

    /**
     * Mod devs should use this to get the actual ChemicalStack.
     *
     * @return The actual ChemicalStack.
     */
    default ChemicalStack getImmutableInternal() {
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
    default boolean isTypeEqual(ICrTChemicalStack stack) {
        return ChemicalStack.isSameChemical(getInternal(), stack.getInternal());
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
    default boolean isEqual(ICrTChemicalStack other) {
        return equals(other);
    }

    /**
     * Casts this chemical stack to a generic {@link ChemicalStackIngredient} for use in recipes that support any chemical type as an input.
     *
     * @apiNote We declare this as generic so that ZenCode can properly match this to the places where we declare all the subtypes as generic.
     */
    @ZenCodeType.Caster(implicit = true)
    default ChemicalStackIngredient asChemicalStackIngredient() {
        return CrTChemicalStackIngredient.from(this);
    }

}