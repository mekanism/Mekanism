package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import org.jetbrains.annotations.NotNull;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

//Note: When wrapping joules to a whole number based energy type we don't need to add any extra simulation steps
// for insert or extract when executing as we will always round down the number and just act upon a lower max requested amount
@NothingNullByDefault
public class FNStrictEnergyHandler implements IStrictEnergyHandler {

    private final IFNEnergyStorage storage;

    public FNStrictEnergyHandler(IFNEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public @Unsigned long getEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getEnergyStoredL()) : FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, @Unsigned long energy) {
        //Not implemented or directly needed
    }

    @Override
    public @Unsigned long getMaxEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(storage.getMaxEnergyStoredL()) : FloatingLong.ZERO;
    }

    @Override
    public @Unsigned long getNeededEnergy(int container) {
        return container == 0 ? EnergyUnit.FORGE_ENERGY.convertFrom(Math.max(0, storage.getMaxEnergyStoredL() - storage.getEnergyStoredL())) : FloatingLong.ZERO;
    }

    @Override
    public @Unsigned long insertEnergy(int container, @Unsigned long amount, @NotNull Action action) {
        return container == 0 ? insertEnergy(amount, action) : amount;
    }

    @Override
    public @Unsigned long insertEnergy(@Unsigned long amount, Action action) {
        if (storage.canReceive()) {
            long toInsert = EnergyUnit.FORGE_ENERGY.convertToAsLong(amount);
            if (toInsert > 0) {
                long inserted = storage.receiveEnergyL(toInsert, action.simulate());
                if (inserted > 0) {
                    //Only bother converting back if any was inserted
                    return amount.subtract(EnergyUnit.FORGE_ENERGY.convertFrom(inserted));
                }
            }
        }
        return amount;
    }

    @Override
    public @Unsigned long extractEnergy(int container, @Unsigned long amount, @NotNull Action action) {
        return container == 0 ? extractEnergy(amount, action) : FloatingLong.ZERO;
    }

    @Override
    public @Unsigned long extractEnergy(@Unsigned long amount, Action action) {
        if (storage.canExtract()) {
            long toExtract = EnergyUnit.FORGE_ENERGY.convertToAsLong(amount);
            if (toExtract > 0) {
                long extracted = storage.extractEnergyL(toExtract, action.simulate());
                return EnergyUnit.FORGE_ENERGY.convertFrom(extracted);
            }
        }
        return FloatingLong.ZERO;
    }
}