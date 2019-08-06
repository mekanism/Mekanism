package mekanism.common.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public interface IFluidHandlerWrapper {

    /**
     * It is assumed that canFill is checked before calling this method
     */
    default int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return 0;
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nullable
    default FluidStack drain(Direction from, @Nonnull FluidStack resource, boolean doDrain) {
        return drain(from, resource.amount, doDrain);
    }

    /**
     * It is assumed that canDrain is checked before calling this method
     */
    @Nullable
    default FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
        return null;
    }

    default boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return false;
    }

    default boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        return false;
    }

    FluidTankInfo[] getTankInfo(Direction from);

    FluidTankInfo[] getAllTanks();
}