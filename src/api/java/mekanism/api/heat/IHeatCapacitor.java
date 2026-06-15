package mekanism.api.heat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
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
    double getHeat();

    @Override
    default double getTemperature() {
        return getHeat() / getHeatCapacity();
    }

    /// Overrides the amount of heat in this [IHeatCapacitor].
    ///
    /// @param heat        Heat to set this capacitor's storage to. **Must be at least 0**.
    /// @param transaction The transaction that this operation is part of if any. May be `null`
    ///
    /// @throws IllegalArgumentException If heat is less than zero. See also [MekanismPreconditions#checkNonNegative(double)] to help perform this check.
    /// @since 10.8.0
    void setHeat(double heat, @Nullable TransactionContext transaction);

    /// Overrides the heat capacity of this [IHeatCapacitor].
    ///
    /// @param newCapacity Heat capacity to set this capacitor to. **Must be at least 1**.
    /// @param transaction The transaction that this operation is part of if any. May be `null`
    ///
    /// @throws IllegalArgumentException If heat capacity is less than one. See also [MekanismPreconditions#checkHeatCapacity(double)] to help perform this check.
    /// @since 10.8.0
    void setHeatCapacity(double newCapacity, @Nullable TransactionContext transaction);

    /// Overrides the heat and heat capacity of this [IHeatCapacitor].
    ///
    /// @param heat         Heat to set this capacitor's storage to. **Must be at least 0**.
    /// @param heatCapacity Heat capacity to set this capacitor to. **Must be at least 1**.
    /// @param transaction  The transaction that this operation is part of if any. May be `null`
    ///
    /// @throws IllegalArgumentException If heat is less than zero, or heat capacity is less than one. See also [MekanismPreconditions#checkNonNegative(double)] and
    /// [MekanismPreconditions#checkHeatCapacity(double)] to help perform this check.
    /// @since 10.8.0
    default void setHeatAndCapacity(double heat, double heatCapacity, @Nullable TransactionContext transaction) {
        try (Transaction subTransaction = Transaction.open(transaction)) {
            setHeatCapacity(heatCapacity, subTransaction);
            setHeat(heat, subTransaction);
            //Ensure that no onContentsChange is fired until after both have been updated (in case the transaction is null)
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
        output.store(SerializationConstants.STATE, CapacitorState.CODEC, new CapacitorState(getHeat(), getHeatCapacity()));
    }

    @Override
    default void deserialize(ValueInput input) {
        input.read(SerializationConstants.STATE, CapacitorState.CODEC).ifPresent(state -> setHeatAndCapacity(state.heat(), state.heatCapacity(), null));
    }

    /// Helper method to copy all pertinent data from another [`heat capacitor`][IHeatCapacitor] to this one without requiring a serialization, deserialization cycle.
    ///
    /// @param other       Capacitor to copy data from.
    /// @param transaction The transaction that this operation is part of. May be `null`, and also the implementation may not fully support rolling back the transaction.
    ///
    /// @implSpec If [#serialize] is overridden, this method should be overridden as well to transfer the relevant data.
    /// @see HeatCapacitorWrapper#getInternal() Getting the internal capacitor when wrapped if instance checks are necessary.
    /// @since 10.8.0
    default void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        setHeatAndCapacity(other.getHeat(), other.getHeatCapacity(), transaction);
    }

    /// Helper record for use in serialization to represent a heat capacitor's state.
    ///
    /// @since 10.8.0
    record CapacitorState(double heat, double heatCapacity) {

        /// Codec for serializing and deserializing a heat capacitor's state.
        public static final Codec<CapacitorState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              SerializerHelper.NON_NEGATIVE_DOUBLE.optionalFieldOf(SerializationConstants.HEAT_STORED, 0D).forGetter(CapacitorState::heat),
              SerializerHelper.ONE_OR_GREATER_DOUBLE.optionalFieldOf(SerializationConstants.HEAT_CAPACITY, 1D).forGetter(CapacitorState::heat)
        ).apply(instance, CapacitorState::new));

        public CapacitorState {
            MekanismPreconditions.checkNonNegative(heat);
            MekanismPreconditions.checkHeatCapacity(heat);
        }
    }
}
