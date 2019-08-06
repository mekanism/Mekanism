package mekanism.common.base.target;

import mekanism.common.base.SplitInfo;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, FluidStack> {

    public FluidHandlerTarget(FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handlers.get(side).fill(PipeUtils.copy(extra, amount), true));
    }

    @Override
    protected Integer simulate(IFluidHandler handler, Direction side, FluidStack fluidStack) {
        return handler.fill(fluidStack, false);
    }
}