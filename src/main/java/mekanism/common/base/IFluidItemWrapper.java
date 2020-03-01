package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@Deprecated
public interface IFluidItemWrapper {

    @Nonnull
    FluidStack getFluid(ItemStack container);

    int getCapacity(ItemStack container);

    int fill(ItemStack container, @Nonnull FluidStack resource, FluidAction fluidAction);

    @Nonnull
    FluidStack drain(ItemStack container, int maxDrain, FluidAction fluidAction);
}