package mekanism.common.base;

import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public interface IFluidHandlerWrapper {

    int fill(EnumFacing from, FluidStack resource, boolean doFill);

    FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain);

    FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain);

    boolean canFill(EnumFacing from, FluidStack fluid);

    boolean canDrain(EnumFacing from, @Nullable FluidStack fluid);

    FluidTankInfo[] getTankInfo(EnumFacing from);

    FluidTankInfo[] getAllTanks();
}
