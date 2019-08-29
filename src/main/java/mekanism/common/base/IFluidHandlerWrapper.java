package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public interface IFluidHandlerWrapper {

    /**
     * It is assumed that canFill is checked before calling this method
     */
    default int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return 0;
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nonnull
    default FluidStack drain(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return drain(from, resource.getAmount(), fluidAction);
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nonnull
    default FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return FluidStack.EMPTY;
    }

    default boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return false;
    }

    default boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return false;
    }

    IFluidTank[] getTankInfo(Direction from);

    IFluidTank[] getAllTanks();
}