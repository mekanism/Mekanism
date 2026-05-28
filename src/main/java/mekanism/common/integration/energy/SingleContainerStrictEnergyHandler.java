package mekanism.common.integration.energy;

import com.google.common.primitives.Ints;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

//Note: When wrapping joules to a whole number based energy type we don't need to add any extra simulation steps
// for insert or extract when executing as we will always round down the number and just act upon a lower max requested amount
@NothingNullByDefault
public abstract class SingleContainerStrictEnergyHandler implements IStrictEnergyHandler {

    protected final IEnergyConversion converter;

    protected SingleContainerStrictEnergyHandler(IEnergyConversion converter) {
        this.converter = converter;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public final int size() {
        return 1;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public final long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return index == 0 ? insert(amount, transaction) : amount;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public final long extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return index == 0 ? extract(amount, transaction) : 0L;
    }

    protected abstract long convertTo(long joules);

    private long convertFromAndBack(long other) {
        long joules = converter.convertFrom(other);
        long result = convertTo(joules);
        double conversion = 1 / converter.getConversion();
        if (conversion >= 1 && result % conversion > 0) {
            return convertTo(joules - 1);
        }
        return result;
    }

    protected abstract long insertCompat(long toInsert, TransactionContext transaction);

    protected abstract long extractCompat(long toExtract, TransactionContext transaction);

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        } else if (converter.isOneToOne()) {
            return insertCompat(amount, transaction);
        }
        long toInsert;
        try (Transaction simulation = Transaction.open(transaction)) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually insert
            long simulatedInserted = insertCompat(converter.convertToAsInt(amount), simulation);
            if (simulatedInserted == 0) {
                //Nothing can be inserted at all, just exit quickly
                return 0;
            }
            //Convert how much we could insert back to Joules so that it gets appropriately clamped so that for example 2 of our compat type gets treated
            // as trying to insert 0 J for how much we actually will accept, and then convert that clamped value to go back to our compat type
            // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
            toInsert = convertFromAndBack(simulatedInserted);
            if (toInsert == 0) {
                //If converting back and forth between Joules and our compat type causes us to be clamped at zero, that means we can't accept anything or could only
                // accept a partial amount; we need to exit early returning that we couldn't insert anything
                return 0;
            }
        }
        return converter.convertFrom(insertCompat(toInsert, transaction));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        } else if (converter.isOneToOne()) {
            return extractCompat(Ints.saturatedCast(amount), transaction);
        }
        long toExtract;
        try (Transaction simulation = Transaction.open(transaction)) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
            long simulatedExtracted = extractCompat(converter.convertToAsInt(amount), simulation);
            if (simulatedExtracted == 0) {
                return 0;
            }
            //Convert how much we could extract back to Joules so that it gets appropriately clamped so that for example 1 Joule gets treated
            // as trying to extract 0 of our compat type for how much we can actually provide, and then convert that clamped value to go back to Joules
            // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided
            // This is important as otherwise if we can have 1.5 Joules extracted, we will reduce our amount by 1.5 Joules but the caller will only receive 1 Joule
            toExtract = convertFromAndBack(simulatedExtracted);
            if (toExtract == 0) {
                //If converting back and forth between Joules and our compat type causes us to be clamped at zero, that means we can't provide anything or could only
                // provide a partial amount; we need to exit early returning that nothing could be extracted
                return 0;
            }
        }
        return converter.convertFrom(extractCompat(toExtract, transaction));
    }
}