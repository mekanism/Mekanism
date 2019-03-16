package mekanism.common.base;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import javax.annotation.Nullable;

public interface IFluidHandlerWrapper 
{
    int fill(EnumFacing from, FluidStack resource, boolean doFill);

    FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain);

    FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain);

    boolean canFill(EnumFacing from, FluidStack fluid);

    boolean canDrain(EnumFacing from, @Nullable FluidStack fluid);
    
    FluidTankInfo[] getTankInfo(EnumFacing from);

    FluidTankInfo[] getAllTanks();
}
