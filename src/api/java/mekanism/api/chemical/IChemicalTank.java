package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NothingNullByDefault
public interface IChemicalTank extends IResourceContainer<ChemicalResource> {

    /**
     * Returns the {@link ChemicalStack} in this tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ChemicalStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED CHEMICAL STACK</em></strong>
     * </p>
     *
     * @return {@link ChemicalStack} in this tank. EMPTY instance of the {@link ChemicalStack} if the tank is empty.
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default ChemicalStack getStack() {
        return getResource().toStack(amountAsLong());
    }

    /**
     * Gets the attribute validator used by this tank. By default, this tank will not allow any chemicals that require validation.
     *
     * @return the tank's attribute validator
     */
    default ChemicalAttributeValidator getAttributeValidator() {
        return ChemicalAttributeValidator.DEFAULT;
    }

    @Override
    @NonExtendable
    default Codec<LargeResourceStack<ChemicalResource>> resourceStackCodec() {
        return SerializerHelper.CHEMICAL_RESOURCE_STACK_CODEC;
    }

    @Override
    @NonExtendable
    default LargeResourceStack<ChemicalResource> emptyStack() {
        return LargeResourceStack.EMPTY_CHEMICAL_STACK;
    }
}