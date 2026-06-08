package mekanism.api.chemical;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

/// Represents a [`resource container`][IResourceContainer] that contains [`chemicals`][ChemicalResource].
@NothingNullByDefault
public interface IChemicalTank extends IResourceContainer<ChemicalResource> {

    /// {@return the attribute validator used by this tank}
    ///
    /// @implNote By default, this tank will not allow any chemicals that require validation.
    default ChemicalAttributeValidator getAttributeValidator() {
        return ChemicalAttributeValidator.DEFAULT;
    }

    @Override
    @NonExtendable
    default LargeResourceStack.StackHelper<ChemicalResource> stackHelper() {
        return LargeResourceStack.CHEMICAL_HELPER;
    }
}