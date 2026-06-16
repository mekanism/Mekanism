package mekanism.api.heat;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface IHeatHandler {

    /// {@return temperature of the handler}
    ///
    /// @implSpec Always bounded by absolute zero (0 degrees kelvin).
    double getTemperature();

    /// Returns the inverse conduction coefficient of the handler. This value defines how much heat is allowed to be dissipated. The larger the number the less heat
    /// can dissipate. The trade-off is that it also allows for lower amounts of heat to be inserted.
    ///
    /// @return Inverse conduction coefficient of the handler. **Must be at least `1`**
    double getInverseConduction();

    /// Returns the heat capacity of the handler. This number can be thought of as the specific heat of the handler (specific heat x mass of the handler).
    ///
    /// @return Heat capacity of the handler. **Must be at least `1`**
    double getHeatCapacity();

    /// Handles a change of heat in this handler. Can be positive or negative.
    ///
    /// @param transfer The amount being transferred.
    void handleHeat(double transfer, TransactionContext transaction);
}