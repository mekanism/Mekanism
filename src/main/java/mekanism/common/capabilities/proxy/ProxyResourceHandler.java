package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyResourceHandler<RESOURCE extends Resource> extends ProxyHandler implements ResourceHandler<RESOURCE> {

    private final IMekanismResourceHandler<RESOURCE, ?> handler;

    public ProxyResourceHandler(IMekanismResourceHandler<RESOURCE, ?> handler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.handler = handler;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public RESOURCE getResource(int index) {
        return handler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, RESOURCE resource) {
        return handler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, RESOURCE resource) {
        return handler.isValid(index, resource);
    }

    @Override
    public int insert(int index, RESOURCE resource, int amount, TransactionContext transaction) {
        return readOnlyInsert() ? 0 : handler.insert(index, resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int insert(RESOURCE resource, int amount, TransactionContext transaction) {
        return readOnlyInsert() ? 0 : handler.insert(resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int extract(int index, RESOURCE resource, int amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0 : handler.extract(index, resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int extract(RESOURCE resource, int amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0 : handler.extract(resource, amount, transaction, AutomationType.handler(side));
    }
}