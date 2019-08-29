package mekanism.common.base;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public interface IFluidItemWrapper {

    FluidStack getFluid(ItemStack container);

    int getCapacity(ItemStack container);

    int fill(ItemStack container, FluidStack resource, FluidAction fluidAction);

    FluidStack drain(ItemStack container, int maxDrain, FluidAction fluidAction);
}