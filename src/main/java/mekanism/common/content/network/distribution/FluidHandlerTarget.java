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

    public FluidHandlerTarget(@NotNull FluidStack type) {
        this.extra = type;
    }

    public FluidHandlerTarget(@NotNull FluidStack type, Collection<IFluidHandler> allHandlers) {
        super(allHandlers);
        this.extra = type;
    }

    public FluidHandlerTarget(@NotNull FluidStack type, int expectedSize) {
        super(expectedSize);
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IFluidHandler handler, SplitInfo splitInfo, long amount) {
        splitInfo.send(handler.fill(extra.copyWithAmount(MathUtils.clampToInt(amount)), FluidAction.EXECUTE));
    }

    @Override
    protected long simulate(IFluidHandler handler, @NotNull FluidStack fluidStack) {
        return handler.fill(fluidStack, FluidAction.SIMULATE);
    }
}