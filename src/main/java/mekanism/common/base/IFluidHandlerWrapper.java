package mekanism.common.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public interface IFluidHandlerWrapper {

    //TODO: Contracts and stuff

    /**
     * It is assumed that canFill is checked before calling this method
     */
    default int fill(EnumFacing from, @Nonnull FluidStack resource, boolean doFill) {
        return 0;
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nullable
    default FluidStack drain(EnumFacing from, @Nonnull FluidStack resource, boolean doDrain) {
        return drain(from, resource.amount, doDrain);
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nullable
    default FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    //TODO: Go through and ensure this is checked against being null before this gets called
    default boolean canFill(EnumFacing from, @Nonnull FluidStack fluid) {
        return false;
    }

    /**
     * null fluid needs to have amounts end up getting specified for draining rather than using the drain with a null stack
     */
    default boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    FluidTankInfo[] getTankInfo(EnumFacing from);

    FluidTankInfo[] getAllTanks();
}