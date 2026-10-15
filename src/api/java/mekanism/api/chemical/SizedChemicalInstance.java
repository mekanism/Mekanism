package mekanism.api.chemical;

import mekanism.api.SerializationConstants;

/// Version of [ChemicalInstance] that also provides access to the amount of the chemical.
///
/// @since 10.8.0
public interface SizedChemicalInstance extends ChemicalInstance {

    String FIELD_AMOUNT = SerializationConstants.AMOUNT;

    /// Gets the size of this chemical instance.
    ///
    /// @return The size of this chemical instance or zero if it is empty
    int amount();
}