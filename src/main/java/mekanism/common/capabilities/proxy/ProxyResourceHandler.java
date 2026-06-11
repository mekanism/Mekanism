package mekanism.common.capabilities.proxy;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ProxyResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends ProxyHandler<IContainerHolder<CONTAINER>> implements ResourceHandler<RESOURCE> {

    private final IMekanismResourceHandler<RESOURCE, CONTAINER> handler;

    public ProxyResourceHandler(IMekanismResourceHandler<RESOURCE, CONTAINER> handler, @Nullable Direction side, IContainerHolder<CONTAINER> holder) {
        super(side, holder);
        this.handler = handler;
    }

    public List<CONTAINER> getProxiedContainers() {
        return handler.getContainers();
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