package mekanism.common.integration.energy.grandpower;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
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
        if (storage.canReceive()) {
            long toInsert = EnergyUnit.FORGE_ENERGY.convertTo(amount);
            if (toInsert > 0) {
                long inserted = storage.receive(toInsert, action.simulate());
                if (inserted > 0) {
                    //Only bother converting back if any was inserted
                    return amount - EnergyUnit.FORGE_ENERGY.convertFrom(inserted);
                }
            }
        }
        return amount;
    }

    @Override
    public long extractEnergy(int container, long amount, @NotNull Action action) {
        return container == 0 ? extractEnergy(amount, action) : 0;
    }

    @Override
    public long extractEnergy(long amount, Action action) {
        if (storage.canExtract()) {
            long toExtract = EnergyUnit.FORGE_ENERGY.convertTo(amount);
            if (toExtract > 0) {
                long extracted = storage.extract(toExtract, action.simulate());
                return EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
            }
        }
        return 0;
    }
}