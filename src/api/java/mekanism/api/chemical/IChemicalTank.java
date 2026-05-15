package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NothingNullByDefault
public interface IChemicalTank extends IResourceContainer<ChemicalResource> {

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