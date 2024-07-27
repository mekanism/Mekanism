package mekanism.common.integration.energy.grandpower;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import org.jetbrains.annotations.NotNull;

//Note: When wrapping joules to a whole number based energy type we don't need to add any extra simulation steps
// for insert or extract when executing as we will always round down the number and just act upon a lower max requested amount
@NothingNullByDefault
public class GPStrictEnergyHandler implements IStrictEnergyHandler {

    private final ILongEnergyStorage storage;

    public GPStrictEnergyHandler(ILongEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public long getEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getAmount()) : 0;
    }

    @Override
    public void setEnergy(int container, long energy) {
        //Not implemented or directly needed
    }

    @Override
    public long getMaxEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getCapacity()) : 0;
    }

    @Override
    public long getNeededEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0, storage.getCapacity() - storage.getAmount())) : 0;
    }

    @Override
    public long insertEnergy(int container, long amount, @NotNull Action action) {
        return container == 0 ? insertEnergy(amount, action) : amount;
    }

    @Override
    public long insertEnergy(long amount, Action action) {
        if (storage.canReceive() && amount > 0) {
            long toInsert = EnergyUnit.FORGE_ENERGY.convertTo(amount);
            if (toInsert == 0) {
                return amount;
            }
            if (!EnergyUnit.FORGE_ENERGY.isOneToOne()) {
                //Before we can actually execute it we need to simulate to calculate how much we can actually insert
                long simulatedInserted = storage.receive(toInsert, true);
                if (simulatedInserted == 0) {
                    //Nothing can be inserted at all, just exit quickly
                    return amount;
                }
                //Convert how much we could insert back to Joules so that it gets appropriately clamped so that for example 2 FE gets treated
                // as trying to insert 0 J for how much we actually will accept, and then convert that clamped value to go back to FE
                // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
                toInsert = convertFromAndBack(simulatedInserted);
                if (toInsert == 0L) {
                    //If converting back and forth between Joules and FE causes us to be clamped at zero, that means we can't accept anything or could only
                    // accept a partial amount; we need to exit early returning that we couldn't insert anything
                    return amount;
                }
            }
            long inserted = storage.receive(toInsert, action.simulate());
            if (inserted > 0) {
                //Only bother converting back if any was inserted
                return amount - EnergyUnit.FORGE_ENERGY.convertFrom(inserted);
            }
        }
        return amount;
    }

    private long convertFromAndBack(long fe) {
        long joules = EnergyUnit.FORGE_ENERGY.convertFrom(fe);
        long result = EnergyUnit.FORGE_ENERGY.convertTo(joules);
        double conversion = 1 / EnergyUnit.FORGE_ENERGY.getConversion();
        if (conversion >= 1 && result % conversion > 0) {
            return EnergyUnit.FORGE_ENERGY.convertTo(joules - 1);
        }
        return result;
    }

    @Override
    public long extractEnergy(int container, long amount, @NotNull Action action) {
        return container == 0 ? extractEnergy(amount, action) : 0;
    }

    @Override
    public long extractEnergy(long amount, Action action) {
        if (storage.canExtract() && amount > 0) {
            long toExtract = EnergyUnit.FORGE_ENERGY.convertTo(amount);
            if (toExtract == 0) {
                return 0;
            }
            if (!EnergyUnit.FORGE_ENERGY.isOneToOne()) {
                //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
                long simulatedExtracted = storage.extract(toExtract, true);
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
            long extracted = storage.extract(toExtract, action.simulate());
            return EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
        }
        return 0;
    }
}