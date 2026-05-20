package mekanism.api.energy;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// A generic container for the transfer and storage of energy whether it be inserting, extracting, querying some value, etc.
@NothingNullByDefault
public interface IEnergyContainer extends ValueIOSerializable, IContentsListener {

    /// Returns the amount of energy in this container, as a `long`.
    ///
    /// The returned amount must be **non-negative**.
    ///
    /// @return the amount of energy in this container, as a long
    @Range(from = 0, to = Long.MAX_VALUE)
    long energy();

    /// Overrides the amount of energy in this [IEnergyContainer].
    ///
    /// @param energy      Energy to set this container's contents to. Must be greater than or equal to 0.
    /// @param transaction The transaction that this operation is part of if any.
    ///
    /// @implNote If the internal amount does get updated make sure to call [#onContentsChanged()]
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
    @Range(from = 0, to = Long.MAX_VALUE)
    long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

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
    @Range(from = 0, to = Long.MAX_VALUE)
    long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

    /// Returns the capacity (maximum amount of energy) of this container, as a `long`.
    ///
    /// The container can be considered full if `amount >= capacity`. Note that the returned capacity may overestimate the actual allowed amount, and it might be smaller
    /// than the current amount. The only way to know if a container will accept a resource, is to try to [`insert`][#insert] it.
    ///
    /// @return the capacity in this container, as a long
    @Range(from = 0, to = Long.MAX_VALUE)
    long capacity();

    /// {@return whether it is generally allowed to be extracted from this container using the given automation type}
    ///
    /// This function serves as a hint on whether energy can be extracted from this container or not. The only way to know if a container will allow the energy to be
    /// extracted, is to try to [`extract`][#extract] it.
    ///
    /// @param automationType The automation type to check.
    default boolean isValidForExtraction(AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

    /// {@return whether it is generally allowed to be inserted into this container using the given automation type}
    ///
    /// This function serves as a hint on whether energy can be inserted into this container or not. The only way to know if a container will accept energy, is to try to
    /// [`insert`][#insert] it.
    ///
    /// @param automationType The automation type to check.
    default boolean isValidForInsertion(AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

    /// Convenience method for checking if this container is empty.
    ///
    /// @return `true` if the container is empty, `false` otherwise.
    default boolean isEmpty() {
        return energy() == 0L;
    }

    /// {@return the amount of energy needed by this energy container to reach a filled state}
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeeded() {
        return Math.max(0L, capacity() - energy());
    }

    @Override
    default void serialize(ValueOutput output) {
        long energy = energy();
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
        setEnergy(other.energy(), null);
    }
}