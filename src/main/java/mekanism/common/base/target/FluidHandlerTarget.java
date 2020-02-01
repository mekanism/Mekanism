package mekanism.common.base.target;

import mekanism.api.annotations.NonNull;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, @NonNull FluidStack> {

    public FluidHandlerTarget(@NonNull FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handlers.get(side).fill(new FluidStack(extra, amount), FluidAction.EXECUTE));
    }

    @Override
    protected Integer simulate(IFluidHandler handler, Direction side, @NonNull FluidStack fluidStack) {
        return handler.fill(fluidStack, FluidAction.SIMULATE);
    }
}