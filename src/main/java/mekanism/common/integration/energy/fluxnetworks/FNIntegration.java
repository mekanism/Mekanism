package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FNIntegration implements IFNEnergyStorage {

    private final IStrictEnergyHandler handler;

    public FNIntegration(IStrictEnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public long receiveEnergyL(long maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        Action action = Action.get(!simulate);
        FloatingLong toInsert = EnergyUnit.FORGE_ENERGY.convertFrom(maxReceive);
        if (action.execute()) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually insert
            FloatingLong simulatedRemainder = handler.insertEnergy(toInsert, Action.SIMULATE);
            if (simulatedRemainder.equals(toInsert)) {
                //Nothing can be inserted at all, just exit quickly
                return 0;
            }
            FloatingLong simulatedInserted = toInsert.subtract(simulatedRemainder);
            //Convert how much we could insert back to FE so that it gets appropriately clamped so that for example 1.5 FE gets treated
            // as trying to insert 1 FE for how much we actually will accept, and then convert that clamped value to go back to Joules
            // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
            toInsert = convertToAndBack(simulatedInserted);
            if (toInsert.isZero()) {
                //If converting back and forth between FE and Joules causes us to be clamped at zero, that means we can't accept anything or could only
                // accept a partial amount; we need to exit early returning that we couldn't insert anything
                return 0;
            }
        }
        FloatingLong remainder = handler.insertEnergy(toInsert, action);
        if (remainder.equals(toInsert)) {
            //Nothing can be inserted at all, just exit quickly
            return 0;
        }
        FloatingLong inserted = toInsert.subtract(remainder);
        return EnergyUnit.FORGE_ENERGY.convertToAsLong(inserted);
    }

    @Override
    public long extractEnergyL(long maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        Action action = Action.get(!simulate);
        FloatingLong toExtract = EnergyUnit.FORGE_ENERGY.convertFrom(maxExtract);
        if (action.execute()) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
            FloatingLong simulatedExtracted = handler.extractEnergy(toExtract, Action.SIMULATE);
            //Convert how much we could extract back to FE so that it gets appropriately clamped so that for example 1.5 FE gets treated
            // as trying to extract 1 FE for how much we can actually provide, and then convert that clamped value to go back to Joules
            // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided
            // This is important as otherwise if we can have 1.5 FE extracted, we will reduce our amount by 1.5 FE but the caller will only receive 1 FE
            toExtract = convertToAndBack(simulatedExtracted);
            if (toExtract.isZero()) {
                //If converting back and forth between FE and Joules causes us to be clamped at zero, that means we can't provide anything or could only
                // provide a partial amount; we need to exit early returning that nothing could be extracted
                return 0;
            }
        }
        FloatingLong extracted = handler.extractEnergy(toExtract, action);
        return EnergyUnit.FORGE_ENERGY.convertToAsLong(extracted);
    }

    private FloatingLong convertToAndBack(FloatingLong value) {
        return EnergyUnit.FORGE_ENERGY.convertFrom(EnergyUnit.FORGE_ENERGY.convertToAsLong(value));
    }

    @Override
    public long getEnergyStoredL() {
        long energy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            long total = EnergyUnit.FORGE_ENERGY.convertToAsLong(handler.getEnergy(container));
            if (total > Long.MAX_VALUE - energy) {
                //Ensure we don't overflow
                return Long.MAX_VALUE;
            }
            energy += total;
        }
        return energy;
    }

    @Override
    public long getMaxEnergyStoredL() {
        long maxEnergy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            long max = EnergyUnit.FORGE_ENERGY.convertToAsLong(handler.getMaxEnergy(container));
            if (max > Long.MAX_VALUE - maxEnergy) {
                //Ensure we don't overflow
                return Long.MAX_VALUE;
            }
            maxEnergy += max;
        }
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        //Mark that we can receive energy if we can insert energy
        if (!handler.extractEnergy(FloatingLong.ONE, Action.SIMULATE).isZero()) {
            return true;
        }
        //Or all our containers are empty. This isn't fully accurate but will give the best
        // accuracy to other mods of if we may be able to extract given we are predicate based
        // instead of having strict can receive checks
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            if (!handler.getEnergy(container).isZero()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canReceive() {
        //Mark that we can receive energy if we can insert energy
        if (handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).smallerThan(FloatingLong.ONE)) {
            return true;
        }
        //Or all our containers are full. This isn't fully accurate but will give the best
        // accuracy to other mods of if we may be able to receive given we are predicate based
        // instead of having strict can receive checks
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            if (!handler.getNeededEnergy(container).isZero()) {
                return false;
            }
        }
        return true;
    }
}