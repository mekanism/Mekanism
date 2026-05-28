package mekanism.common.integration.energy;

import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IStrictEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class BaseEnergyIntegration {

    protected final IStrictEnergyHandler handler;
    protected final IEnergyConversion converter;

    protected BaseEnergyIntegration(IStrictEnergyHandler handler, IEnergyConversion converter) {
        this.handler = handler;
        this.converter = converter;
    }

    protected abstract long convertTo(long joules);

    private long convertToAndBack(long joules) {
        long other = convertTo(joules);
        long result = converter.convertFrom(other);
        if (converter.getConversion() >= 1 && result % converter.getConversion() > 0) {
            return converter.convertFrom(other - 1);
        }
        return result;
    }

    protected long calculateToInsert(long amount, TransactionContext transaction) {
        //Note: We know that we aren't 1:1 when this method is called, so we have to see if we are limited in how much we can insert by the conversions
        try (Transaction simulation = Transaction.open(transaction)) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually insert
            long simulatedInserted = handler.insert(converter.convertFrom(amount), simulation);
            if (simulatedInserted == 0) {
                //Nothing can be inserted at all, just exit quickly
                return 0;
            }
            //Convert how much we could insert back to our compat type so that it gets appropriately clamped so that for example 1.5 of our compat type gets treated
            // as trying to insert 1 of our compat type for how much we actually will accept, and then convert that clamped value to go back to Joules
            // so that we don't allow inserting a tiny bit of extra for "free" and end up creating power from nowhere
            return convertToAndBack(simulatedInserted);
        }
    }

    protected long calculateToExtract(long amount, TransactionContext transaction) {
        //Note: We know that we aren't 1:1 when this method is called, so we have to see if we are limited in how much we can extract by the conversions
        try (Transaction simulation = Transaction.open(transaction)) {
            //Before we can actually execute it we need to simulate to calculate how much we can actually extract in our other units
            long simulatedExtracted = handler.extract(converter.convertFrom(amount), simulation);
            if (simulatedExtracted == 0) {
                return 0;
            }
            //Convert how much we could extract back to our compat type so that it gets appropriately clamped so that for example 1.5 of our compat type gets treated
            // as trying to extract 1 of our compat type for how much we can actually provide, and then convert that clamped value to go back to Joules
            // so that we don't allow extracting a tiny bit into nowhere causing some power to be voided. This is important as otherwise if we can have 1.5 of
            // our compat type extracted, we will reduce our amount by 1.5 of our compat type but the caller will only receive 1 of our compat type
            return convertToAndBack(simulatedExtracted);
        }
    }

    protected long calculateSum(EnergyGetter getter) {
        long energy = 0;
        for (int container = 0, containers = handler.size(); container < containers; container++) {
            long max = converter.convertTo(getter.get(handler, container));
            if (max > Long.MAX_VALUE - energy) {
                //Ensure we don't overflow
                return Long.MAX_VALUE;
            }
            energy += max;
        }
        return energy;
    }

    @FunctionalInterface
    protected interface EnergyGetter {

        long get(IStrictEnergyHandler handler, int container);
    }
}