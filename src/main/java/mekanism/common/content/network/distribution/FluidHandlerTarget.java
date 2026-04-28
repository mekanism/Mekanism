package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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
    protected void acceptAmount(ResourceHandler<FluidResource> handler, SplitInfo splitInfo, @NotNull FluidStack resource, long amount) {
        //TODO - 26.1: Remove this and replace it with proper handling of resource handlers
        IFluidHandler legacyHandler = IFluidHandler.of(handler);
        splitInfo.send(legacyHandler.fill(resource.copyWithAmount(Ints.saturatedCast(amount)), FluidAction.EXECUTE));
    }

    @Override
    protected long simulate(ResourceHandler<FluidResource> handler, @NotNull FluidStack resource, long amount) {
        //TODO - 26.1: Remove this and replace it with proper handling of resource handlers
        IFluidHandler legacyHandler = IFluidHandler.of(handler);
        return legacyHandler.fill(resource.copyWithAmount(Ints.saturatedCast(amount)), FluidAction.SIMULATE);
    }
}