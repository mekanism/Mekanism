package mekanism.common.base.target;

import javax.annotation.Nonnull;
import mekanism.common.base.SplitInfo;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidHandlerTarget extends Target<IFluidHandler, Integer, FluidStack> {

    public FluidHandlerTarget(@Nonnull FluidStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handlers.get(side).fill(PipeUtils.copy(extra, amount), FluidAction.EXECUTE));
    }

    @Override
    protected Integer simulate(IFluidHandler handler, Direction side, @Nonnull FluidStack fluidStack) {
        return handler.fill(fluidStack, FluidAction.SIMULATE);
    }
}