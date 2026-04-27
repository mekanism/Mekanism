package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.VisibleForTesting;

@NothingNullByDefault
public class ForgeEnergyIntegration implements EnergyHandler {

    private final EnergyJournal energyJournal = new EnergyJournal();
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
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        }
        long toInsert = converter.convertFrom(amount);
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
        energyJournal.updateSnapshots(transaction);
        long remainder = handler.insertEnergy(toInsert, Action.EXECUTE);
        if (remainder == toInsert) {
            //Nothing can be inserted at all, just exit quickly
            return 0;
        }
        long inserted = toInsert - remainder;
        return converter.convertToAsInt(inserted);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) {
            return 0;
        }
        long toExtract = converter.convertFrom(amount);
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
        energyJournal.updateSnapshots(transaction);
        long extracted = handler.extractEnergy(toExtract, Action.EXECUTE);
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
    public long getAmountAsLong() {
        long energy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            long total = converter.convertTo(handler.getEnergy(container));
            if (total > Long.MAX_VALUE - energy) {
                //Ensure we don't overflow
                return Long.MAX_VALUE;
            }
            energy += total;
        }
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        long maxEnergy = 0;
        for (int container = 0, containers = handler.getEnergyContainerCount(); container < containers; container++) {
            long max = converter.convertTo(handler.getMaxEnergy(container));
            if (max > Long.MAX_VALUE - maxEnergy) {
                //Ensure we don't overflow
                return Long.MAX_VALUE;
            }
            maxEnergy += max;
        }
        return maxEnergy;
    }

    //TODO - 26.1: Remove this and just have strict energy handlers natively support transactions so then we don't have to manually handle simulation
    private class EnergyJournal extends SnapshotJournal<Long> {

        @Override
        protected Long createSnapshot() {
            //TODO: Theoretically this should be made to support multiple containers, but as this is only temporary until we make strict energy support transactions
            // it doesn't matter
            return handler.getEnergy(0);
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            //Note: Not 100% safe, but we don't have any other option for now
            handler.setEnergy(0, snapshot);
        }
    }
}