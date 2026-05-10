package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyStrictEnergyHandler extends ProxyHandler implements IStrictEnergyHandler {

    private final IMekanismStrictEnergyHandler handler;

    public ProxyStrictEnergyHandler(IMekanismStrictEnergyHandler handler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.handler = handler;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    @Override
    public void setEnergy(int container, long energy) {//TODO - 26.1: Re-evaluate this being exposed to strict energy handler instead of just the more internal one
        if (!readOnly) {
            handler.setEnergy(container, energy);
        }
    }

    @Override
    public long getCapacityAsLong(int index) {
        return handler.getCapacityAsLong(index);
    }

    @Override
    public long getNeededEnergy(int container) {
        return handler.getNeededEnergy(container);
    }

    @Override
    public long insert(int index, long amount, TransactionContext transaction) {
        return readOnlyInsert() ? amount : handler.insert(index, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public long insert(long amount, TransactionContext transaction) {
        return readOnlyInsert() ? amount : handler.insert(amount, transaction, AutomationType.handler(side));
    }

    @Override
    public long extract(int index, long amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0L : handler.extract(index, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public long extract(long amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0L : handler.extract(amount, transaction, AutomationType.handler(side));
    }
}