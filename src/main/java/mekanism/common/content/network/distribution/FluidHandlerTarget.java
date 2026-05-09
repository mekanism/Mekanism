package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerTarget extends Target<ResourceHandler<FluidResource>, @NotNull FluidStack> {

    public FluidHandlerTarget() {
    }

    public FluidHandlerTarget(Collection<ResourceHandler<FluidResource>> allHandlers) {
        super(allHandlers);
    }

    public FluidHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(ResourceHandler<FluidResource> handler, SplitInfo splitInfo, FluidStack resource, long amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            splitInfo.send(handler.insert(FluidResource.of(resource), Ints.saturatedCast(amount), transaction));
            transaction.commit();
        }
    }

    @Override
    protected long simulate(ResourceHandler<FluidResource> handler, FluidStack resource, long amount) {
        try (Transaction simulation = Transaction.openRoot()) {
            return handler.insert(FluidResource.of(resource), Ints.saturatedCast(amount), simulation);
        }
    }
}