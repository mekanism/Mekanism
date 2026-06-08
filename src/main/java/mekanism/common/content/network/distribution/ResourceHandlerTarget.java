package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ResourceHandlerTarget<RESOURCE extends Resource> extends Target<ResourceHandler<RESOURCE>, RESOURCE> {

    public ResourceHandlerTarget(Collection<ResourceHandler<RESOURCE>> allHandlers) {
        super(allHandlers);
    }

    public ResourceHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(ResourceHandler<RESOURCE> handler, RESOURCE resource, long amount, TransactionContext transaction) {
        return handler.insert(resource, Ints.saturatedCast(amount), transaction);
    }
}