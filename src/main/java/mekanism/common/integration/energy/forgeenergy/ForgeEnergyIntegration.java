package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.VisibleForTesting;

public class ForgeEnergyIntegration implements IEnergyStorage {

    private final IStrictEnergyHandler handler;
    private final IEnergyConversion converter;

    public ForgeEnergyIntegration(IStrictEnergyHandler handler) {
        this(handler, EnergyUnit.FORGE_ENERGY);
    }

    @VisibleForTesting
    ForgeEnergyIntegration(IStrictEnergyHandler handler, IEnergyConversion converter) {
        this.handler = handler;
        this.converter = converter;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        Action action = Action.get(!simulate);
        long toInsert = converter.convertFrom(maxReceive);
        if (toInsert == 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually insert
            long simulatedRemainder = handler.insertEnergy(toInsert, Action.SIMULATE);
            if (simulatedRemainder == toInsert) {
                //Nothing can be inserted at all, just exit quickly
                return 0;
            }
            long simulatedInserted = toInsert - simulatedRemainder;
            //Convert how much we could insert back to FE so that it gets appropriately clamped so that for example 1.5 FE gets treated
            // as trying to insert 1 FE for how much we actually will accept, and then convert that clamped value to go back to Joules
            // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
            toInsert = convertToAndBack(simulatedInserted);
            if (toInsert == 0L) {
                //If converting back and forth between FE and Joules causes us to be clamped at zero, that means we can't accept anything or could only
                // accept a partial amount; we need to exit early returning that we couldn't insert anything
                return 0;
            }
        }
        long remainder = handler.insertEnergy(toInsert, action);
        if (remainder == toInsert) {
            //Nothing can be inserted at all, just exit quickly
            return 0;
        }
        long inserted = toInsert - remainder;
        return converter.convertToAsInt(inserted);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) {
            return 0;
        }
        Action action = Action.get(!simulate);
        long toExtract = converter.convertFrom(maxExtract);
        if (toExtract == 0) {
            return 0;
        }
        if (!converter.isOneToOne()) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
            long simulatedExtracted = handler.extractEnergy(toExtract, Action.SIMULATE);
            //Convert how much we could extract back to FE so that it gets appropriately clamped so that for example 1.5 FE gets treated
            // as trying to extract 1 FE for how much we can actually provide, and then convert that clamped value to go back to Joules
            // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided
            // This is important as otherwise if we can have 1.5 FE extracted, we will reduce our amount by 1.5 FE but the caller will only receive 1 FE
            toExtract = convertToAndBack(simulatedExtracted);
            if (toExtract == 0L) {
                //If converting back and forth between FE and Joules causes us to be clamped at zero, that means we can't provide anything or could only
                // provide a partial amount; we need to exit early returning that nothing could be extracted
                return 0;
            }
        }
        long extracted = handler.extractEnergy(toExtract, action);
        return converter.convertToAsInt(extracted);
    }

    private long convertToAndBack(long joules) {
        int fe = converter.convertToAsInt(joules);
        long result = converter.convertFrom(fe);
        if (converter.getConversion() >= 1 && result % converter.getConversion() > 0) {
            return converter.convertFrom(fe - 1);
        }
        return result;
    }

    @Override
    public int getEnergyStored() {
        int energy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            int total = converter.convertToAsInt(handler.getEnergy(container));
            if (total > Integer.MAX_VALUE - energy) {
                //Ensure we don't overflow
                return Integer.MAX_VALUE;
            }
            energy += total;
        }
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        int maxEnergy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            int max = converter.convertToAsInt(handler.getMaxEnergy(container));
            if (max > Integer.MAX_VALUE - maxEnergy) {
                //Ensure we don't overflow
                return Integer.MAX_VALUE;
            }
            maxEnergy += max;
        }
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}