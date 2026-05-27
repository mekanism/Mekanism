package mekanism.api.energy;

import com.google.common.primitives.Ints;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public interface IStrictEnergyHandler {//TODO - 26.1: Redo the documentation for this class

    @Range(from = 0, to = Integer.MAX_VALUE)
    int size();

    @Range(from = 0, to = Long.MAX_VALUE)
    long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index);

    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getAmountAsInt(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return Ints.saturatedCast(getAmountAsLong(index));
    }

    //TODO - 26.1: Do we want to name this getLimit to be closer to the resource handlers?
    @Range(from = 0, to = Long.MAX_VALUE)
    long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index);

    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getCapacityAsInt(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return Ints.saturatedCast(getCapacityAsLong(index));
    }

    /**
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param index Container to query.
     *
     * @return The energy needed to fill the container.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return Math.max(0L, getCapacityAsLong(index) - getAmountAsLong(index));
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction);

    @Range(from = 0, to = Long.MAX_VALUE)
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);

        long inserted = 0;
        for (int index = 0, size = size(); index < size; index++) {
            inserted += insert(index, amount - inserted, transaction);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    long extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction);

    @Range(from = 0, to = Long.MAX_VALUE)
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        long extracted = 0;
        for (int index = 0, size = size(); index < size; index++) {
            extracted += extract(index, amount - extracted, transaction);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }
}