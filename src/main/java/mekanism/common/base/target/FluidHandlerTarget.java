package mekanism.common.base.target;

import mekanism.common.base.SplitInfo;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, FluidStack> {

    public FluidHandlerTarget(FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(EnumFacing side, SplitInfo<Integer> splitInfo, Integer amount) {
        //Give it fluid and add how much actually got accepted instead of how much
        // we attempted to give it
        splitInfo.send(handlers.get(side).fill(PipeUtils.copy(extra, amount), true));
    }

    @Override
    protected Integer simulate(IFluidHandler handler, EnumFacing side, FluidStack fluidStack) {
        return handler.fill(fluidStack, false);
    }
}