package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.VisibleForTesting;

//Note: When wrapping joules to a whole number based energy type we don't need to add any extra simulation steps
// for insert or extract when executing as we will always round down the number and just act upon a lower max requested amount
@NothingNullByDefault
public class ForgeStrictEnergyHandler implements IStrictEnergyHandler {

    private final EnergyHandler neoHandler;
    private final IEnergyConversion converter;

    public ForgeStrictEnergyHandler(EnergyHandler neoHandler) {
        this(neoHandler, EnergyUnit.FORGE_ENERGY);
    }

    @VisibleForTesting
    ForgeStrictEnergyHandler(EnergyHandler neoHandler, IEnergyConversion converter) {
        this.neoHandler = neoHandler;
        this.converter = converter;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int size() {
        return 1;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        //TODO - 26.1: Should this be using the int versions of amount?
        return container == 0 ? converter.convertFrom(neoHandler.getAmountAsLong()) : 0L;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        //TODO - 26.1: Should this be using the int versions of capacity?
        return container == 0 ? converter.convertFrom(neoHandler.getCapacityAsLong()) : 0L;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getNeededEnergy(@Range(from = 0, to = Integer.MAX_VALUE) int container) {
        //TODO - 26.1: Should this be using the int versions of capacity and amount?
        return container == 0 ? converter.convertFrom(Math.max(0, neoHandler.getCapacityAsLong() - neoHandler.getAmountAsLong())) : 0L;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return index == 0 ? insert(amount, transaction) : amount;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return amount;
        }
        int toInsert = converter.convertToAsInt(amount);
        if (toInsert == 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //TODO - 26.1: See if we can entirely skip having to do simulation
            try (Transaction simulation = Transaction.open(transaction)) {
                //Before we can actually execute it we need to simulate to calculate how much we can actually insert
                long simulatedInserted = neoHandler.insert(toInsert, simulation);
                if (simulatedInserted == 0) {
                    //Nothing can be inserted at all, just exit quickly
                    return 0;
                }
                //Convert how much we could insert back to Joules so that it gets appropriately clamped so that for example 2 FE gets treated
                // as trying to insert 0 J for how much we actually will accept, and then convert that clamped value to go back to FE
                // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
                toInsert = convertFromAndBack(simulatedInserted);
                if (toInsert == 0L) {
                    //If converting back and forth between Joules and FE causes us to be clamped at zero, that means we can't accept anything or could only
                    // accept a partial amount; we need to exit early returning that we couldn't insert anything
                    return 0;
                }
            }
        }
        int inserted = neoHandler.insert(toInsert, transaction);
        //Only bother converting back if any was inserted
        return inserted == 0 ? 0 : converter.convertFrom(inserted);
    }

    private int convertFromAndBack(long fe) {
        long joules = converter.convertFrom(fe);
        int result = converter.convertToAsInt(joules);
        double conversion = 1 / converter.getConversion();
        if (conversion >= 1 && result % conversion > 0) {
            return converter.convertToAsInt(joules - 1);
        }
        return result;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return index == 0 ? extract(amount, transaction) : 0L;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0L;
        }
        int toExtract = converter.convertToAsInt(amount);
        if (toExtract == 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //TODO - 26.1: See if we can entirely skip having to do simulation
            try (Transaction simulation = Transaction.open(transaction)) {
                //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
                long simulatedExtracted = neoHandler.extract(toExtract, simulation);
                //Convert how much we could extract back to Joules so that it gets appropriately clamped so that for example 1 Joule gets treated
                // as trying to extract 0 FE for how much we can actually provide, and then convert that clamped value to go back to Joules
                // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided
                // This is important as otherwise if we can have 1.5 Joules extracted, we will reduce our amount by 1.5 Joules but the caller will only receive 1 Joule
                toExtract = convertFromAndBack(simulatedExtracted);
                if (toExtract == 0L) {
                    //If converting back and forth between Joules and FE causes us to be clamped at zero, that means we can't provide anything or could only
                    // provide a partial amount; we need to exit early returning that nothing could be extracted
                    return 0;
                }
            }
        }
        int extracted = neoHandler.extract(toExtract, transaction);
        return extracted == 0 ? 0 : converter.convertFrom(extracted);
    }
}