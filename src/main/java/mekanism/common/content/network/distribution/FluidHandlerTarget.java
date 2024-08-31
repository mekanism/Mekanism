package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerTarget extends Target<IFluidHandler, @NotNull FluidStack> {

    public FluidHandlerTarget() {
    }

    public FluidHandlerTarget(Collection<IFluidHandler> allHandlers) {
        super(allHandlers);
    }

    public FluidHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(IFluidHandler handler, SplitInfo splitInfo, @NotNull FluidStack resource, long amount) {
        splitInfo.send(handler.fill(resource.copyWithAmount(MathUtils.clampToInt(amount)), FluidAction.EXECUTE));
    }

    @Override
    protected long simulate(IFluidHandler handler, @NotNull FluidStack resource, long amount) {
        return handler.fill(resource.copyWithAmount(MathUtils.clampToInt(amount)), FluidAction.SIMULATE);
    }
}