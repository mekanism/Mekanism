package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public long getEnergy(int container) {
        //TODO - 26.1: Should this be using the int versions of amount?
        return container == 0 ? converter.convertFrom(neoHandler.getAmountAsLong()) : 0L;
    }

    @Override
    public void setEnergy(int container, long energy) {
        //Not implemented or directly needed
    }

    @Override
    public long getMaxEnergy(int container) {
        //TODO - 26.1: Should this be using the int versions of capacity?
        return container == 0 ? converter.convertFrom(neoHandler.getCapacityAsLong()) : 0L;
    }

    @Override
    public long getNeededEnergy(int container) {
        //TODO - 26.1: Should this be using the int versions of capacity and amount?
        return container == 0 ? converter.convertFrom(Math.max(0, neoHandler.getCapacityAsLong() - neoHandler.getAmountAsLong())) : 0L;
    }

    @Override
    public long insertEnergy(int container, long amount, Action action) {
        return container == 0 ? insertEnergy(amount, action) : amount;
    }

    @Override
    public long insertEnergy(long amount, Action action) {
        if (amount > 0) {
            int toInsert = converter.convertToAsInt(amount);
            if (toInsert == 0) {
                return amount;
            }
            if (!converter.isOneToOne()) {
                //TODO - 26.1: See if we can entirely skip having to do simulation
                try (Transaction tx = Transaction.openRoot()) {
                    //Before we can actually execute it we need to simulate to calculate how much we can actually insert
                    long simulatedInserted = neoHandler.insert(toInsert, tx);
                    if (simulatedInserted == 0) {
                        //Nothing can be inserted at all, just exit quickly
                        return amount;
                    }
                    //Convert how much we could insert back to Joules so that it gets appropriately clamped so that for example 2 FE gets treated
                    // as trying to insert 0 J for how much we actually will accept, and then convert that clamped value to go back to FE
                    // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
                    toInsert = convertFromAndBack(simulatedInserted);
                }
                if (toInsert == 0L) {
                    //If converting back and forth between Joules and FE causes us to be clamped at zero, that means we can't accept anything or could only
                    // accept a partial amount; we need to exit early returning that we couldn't insert anything
                    return amount;
                }
            }
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = neoHandler.insert(toInsert, tx);
                if (inserted > 0) {
                    if (action.execute()) {
                        tx.commit();
                    }
                    //Only bother converting back if any was inserted
                    return amount - converter.convertFrom(inserted);
                }
            }
        }
        return amount;
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
    public long extractEnergy(int container, long amount, Action action) {
        return container == 0 ? extractEnergy(amount, action) : 0L;
    }

    @Override
    public long extractEnergy(long amount, Action action) {
        if (amount > 0) {
            int toExtract = converter.convertToAsInt(amount);
            if (toExtract == 0) {
                return 0;
            }
            if (!converter.isOneToOne()) {
                //TODO - 26.1: See if we can entirely skip having to do simulation
                try (Transaction tx = Transaction.openRoot()) {
                    //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
                    long simulatedExtracted = neoHandler.extract(toExtract, tx);
                    //Convert how much we could extract back to Joules so that it gets appropriately clamped so that for example 1 Joule gets treated
                    // as trying to extract 0 FE for how much we can actually provide, and then convert that clamped value to go back to Joules
                    // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided
                    // This is important as otherwise if we can have 1.5 Joules extracted, we will reduce our amount by 1.5 Joules but the caller will only receive 1 Joule
                    toExtract = convertFromAndBack(simulatedExtracted);
                }
                if (toExtract == 0L) {
                    //If converting back and forth between Joules and FE causes us to be clamped at zero, that means we can't provide anything or could only
                    // provide a partial amount; we need to exit early returning that nothing could be extracted
                    return 0;
                }
            }
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = neoHandler.extract(toExtract, tx);
                if (action.execute()) {
                    tx.commit();
                }
                return converter.convertFrom(extracted);
            }
        }
        return 0L;
    }
}