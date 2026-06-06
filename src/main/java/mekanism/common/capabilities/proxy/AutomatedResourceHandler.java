package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class AutomatedResourceHandler<RESOURCE extends Resource> implements ResourceHandler<RESOURCE> {

    @Nullable
    public static <RESOURCE extends Resource> ResourceHandler<RESOURCE> wrap(@Nullable ResourceHandler<RESOURCE> handler, AutomationType automationType) {
        return handler == null ? null : new AutomatedResourceHandler<>(handler, automationType);
    }

    @Nullable
    private final IMekanismResourceHandler<RESOURCE, ?> mekHandler;
    private final ResourceHandler<RESOURCE> handler;
    private final AutomationType automationType;

    private AutomatedResourceHandler(ResourceHandler<RESOURCE> handler, AutomationType automationType) {
        this.handler = handler;
        if (this.handler instanceof IMekanismResourceHandler<RESOURCE, ?> mek) {
            this.mekHandler = mek;
        } else {
            this.mekHandler = null;
        }
        this.automationType = automationType;
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
        if (mekHandler == null) {
            return handler.insert(index, resource, amount, transaction);
        }
        return mekHandler.insert(index, resource, amount, transaction, automationType);
    }

    @Override
    public int insert(RESOURCE resource, int amount, TransactionContext transaction) {
        if (mekHandler == null) {
            return handler.insert(resource, amount, transaction);
        }
        return mekHandler.insert(resource, amount, transaction, automationType);
    }

    @Override
    public int extract(int index, RESOURCE resource, int amount, TransactionContext transaction) {
        if (mekHandler == null) {
            return handler.extract(index, resource, amount, transaction);
        }
        return mekHandler.extract(index, resource, amount, transaction, automationType);
    }

    @Override
    public int extract(RESOURCE resource, int amount, TransactionContext transaction) {
        if (mekHandler == null) {
            return handler.extract(resource, amount, transaction);
        }
        return mekHandler.extract(resource, amount, transaction, automationType);
    }
}