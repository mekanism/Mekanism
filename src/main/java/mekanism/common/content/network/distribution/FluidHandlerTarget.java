package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, @NotNull FluidStack> {

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
    protected void acceptAmount(IFluidHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handler.fill(extra.copyWithAmount(amount), FluidAction.EXECUTE));
    }

    @Override
    protected Integer simulate(IFluidHandler handler, @NotNull FluidStack fluidStack) {
        return handler.fill(fluidStack, FluidAction.SIMULATE);
    }
}