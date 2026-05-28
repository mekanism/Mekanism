package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.IContainerHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ProxyStrictEnergyHandler extends ProxyHandler implements IStrictEnergyHandler {

    private final IMekanismStrictEnergyHandler handler;

    public ProxyStrictEnergyHandler(IMekanismStrictEnergyHandler handler, @Nullable Direction side, @Nullable IContainerHolder<IEnergyContainer> holder) {
        super(side, holder);
        this.handler = handler;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int size() {
        return handler.size();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.getAmountAsLong(index);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.getCapacityAsLong(index);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return readOnlyInsert() ? amount : handler.insert(index, amount, transaction, AutomationType.handler(side));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return readOnlyInsert() ? amount : handler.insert(amount, transaction, AutomationType.handler(side));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0L : handler.extract(index, amount, transaction, AutomationType.handler(side));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0L : handler.extract(amount, transaction, AutomationType.handler(side));
    }
}