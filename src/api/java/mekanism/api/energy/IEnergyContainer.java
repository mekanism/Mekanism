package mekanism.api.energy;

import com.google.common.primitives.Ints;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// A generic container for the transfer and storage of energy whether it be inserting, extracting, querying some value, etc.
@NothingNullByDefault
public interface IEnergyContainer extends ValueIOSerializable, EnergyHandler {//TODO - 26.1: Add docs for methods that are missing them

    /// Overrides the amount of energy in this [IEnergyContainer].
    ///
    /// @param energy      Energy to set this container's contents to. Must be greater than or equal to 0.
    /// @param transaction The transaction that this operation is part of if any.
    void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy, @Nullable TransactionContext transaction);

    /// Inserts up to the given amount of energy into this container.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
    ///
    /// @param amount         The maximum amount of energy to insert. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was inserted. Between `0` (inclusive, nothing was inserted) and `amount` (inclusive, everything was inserted).
    ///
    /// @throws IllegalArgumentException If the amount is negative. See also [MekanismPreconditions#checkNonNegative] to help perform this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// energy container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    @Override
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return insert(amount, transaction, defaultAutomationType());
    }

    /// Tries to extract up to the given amount of energy from this container.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
    ///
    /// @param amount         The maximum amount of energy to extract. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @throws IllegalArgumentException If the amount is negative. See also [MekanismPreconditions#checkNonNegative] to help perform this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// energy container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    @Override
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(amount, transaction, defaultAutomationType());
    }

    /// {@return whether it is generally allowed for energy to be extracted from this container using the given automation type}
    ///
    /// This function serves as a hint on whether energy can be extracted from this container or not. The only way to know if a container will allow the energy to be
    /// extracted, is to try to [`extract`][#extract] it.
    ///
    /// @param automationType The automation type to check.
    default boolean isValidForExtraction(AutomationType automationType) {
        return true;
    }

    /// {@return whether it is generally allowed for energy to be inserted into this container using the given automation type}
    ///
    /// This function serves as a hint on whether energy can be inserted into this container or not. The only way to know if a container will accept energy, is to try to
    /// [`insert`][#insert] it.
    ///
    /// @param automationType The automation type to check.
    default boolean isValidForInsertion(AutomationType automationType) {
        return true;
    }

    /// Convenience method for checking if this container is empty.
    ///
    /// @return `true` if the container is empty, `false` otherwise.
    @NonExtendable
    default boolean isEmpty() {
        return getAmountAsLong() == 0L;
    }

    /// {@return the amount of energy needed by this energy container to reach a filled state}
    @NonExtendable
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededAsLong() {
        return Math.max(0, getCapacityAsLong() - getAmountAsLong());
    }

    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getNeededAsInt() {
        return Ints.saturatedCast(getNeededAsLong());
    }

    @Override
    default void serialize(ValueOutput output) {
        long energy = getAmountAsLong();
        if (energy > 0) {
            output.putLong(SerializationConstants.STORED, energy);
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        setEnergy(input.getLongOr(SerializationConstants.STORED, 0), null);
    }

    /// Helper method to copy all pertinent data from another [`energy container`][IEnergyContainer] to this one without requiring a serialization, deserialization
    /// cycle.
    ///
    /// @param other Container to copy data from.
    ///
    /// @implSpec If [#serialize] is overridden, this method should be overridden as well to transfer the relevant data.
    /// @since 10.8.0
    default void copyContents(IEnergyContainer other) {
        setEnergy(other.getAmountAsLong(), null);
    }

    /// Determines which automation type methods defined via [EnergyHandler] methods will use.
    private AutomationType defaultAutomationType() {
        return AutomationType.EXTERNAL;
    }
}