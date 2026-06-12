package mekanism.api.heat;

import mekanism.api.SerializationConstants;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IHeatCapacitor extends ValueIOSerializable, IHeatHandler {

    /// {@return the inverse insulation coefficient for this capacitor} The larger the value the less heat dissipates into the environment.
    double getInverseInsulation();

    /// {@return heat stored in this capacitor}
    double getHeat();//TODO - 26.1 (heat): Should this be moved into IHeatHandler?

    /// Overrides the amount of heat in this [IHeatCapacitor].
    ///
    /// @param heat        Heat to set this capacitor's storage to. May be `0`.
    /// @param transaction The transaction that this operation is part of if any.
    /// @implSpec If a value less than zero is passed, it should be clamped to zero.
    ///
    /// @since 10.8.0
    void setHeat(double heat, @Nullable TransactionContext transaction);

    //TODO - 26.1 (heat): Docs
    void setHeatCapacity(double newCapacity, @Nullable TransactionContext transaction);

    //TODO - 26.1 (heat): Docs
    default void setHeatAndCapacity(double heat, double heatCapacity, @Nullable TransactionContext transaction) {
        try (Transaction subTransaction = Transaction.open(transaction)) {
            setHeatCapacity(heatCapacity, subTransaction);
            setHeat(heat, subTransaction);
            //Ensure that no onContentsChange is fired until after both have been updated
            subTransaction.commit();
        }
    }

    /// Checks if this heat capacitor is currently at the ambient temperature of its surroundings.
    ///
    /// @return `true` if this capacitor is currently at the ambient temperature of the environment it is in.
    ///
    /// @implNote This method should be overridden to take into account any variable ambient temperature data such as biomes.
    /// @since 10.7.15
    default boolean isAmbientTemperature() {
        return Mth.equal(getTemperature(), HeatAPI.AMBIENT_TEMP);
    }

    @Override
    default void serialize(ValueOutput output) {
        output.putDouble(SerializationConstants.HEAT_CAPACITY, getHeatCapacity());
        output.putDouble(SerializationConstants.STORED, getHeat());
    }

    @Override
    default void deserialize(ValueInput input) {
        setHeatAndCapacity(input.getDoubleOr(SerializationConstants.STORED, getHeat()),
              input.getDoubleOr(SerializationConstants.HEAT_CAPACITY, getHeatCapacity()), null);
    }

    /// Helper method to copy all pertinent data from another [`heat capacitor`][IHeatCapacitor] to this one without requiring a serialization, deserialization cycle.
    ///
    /// @param other Capacitor to copy data from.
    /// @param transaction The transaction that this operation is part of. May be `null`, and also the implementation may not fully support rolling back the transaction.
    ///
    /// @implSpec If [#serialize] is overridden, this method should be overridden as well to transfer the relevant data.
    /// @see HeatCapacitorWrapper#getInternal() Getting the internal capacitor when wrapped if instance checks are necessary.
    /// @since 10.8.0
    default void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        setHeatAndCapacity(other.getHeat(), other.getHeatCapacity(), transaction);
    }
}
