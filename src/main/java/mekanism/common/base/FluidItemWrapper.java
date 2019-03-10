package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidItemWrapper extends ItemCapability implements IFluidHandlerItem {

    @Override
    public FluidTankProperties[] getTankProperties() {
        return new FluidTankProperties[]{
              new FluidTankProperties(getItem().getFluid(getStack()), getItem().getCapacity(getStack()))};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (getStack().getCount() != 1) {
            return 0;
        }

        return getItem().fill(getStack(), resource, doFill);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (getStack().getCount() != 1 || resource == null) {
            return null;
        }

        FluidStack canDrain = drain(resource.amount, false);

        if (canDrain != null) {
            if (canDrain.isFluidEqual(resource)) {
                return drain(resource.amount, doDrain);
            }
        }

        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (getStack().getCount() != 1) {
            return null;
        }

        return getItem().drain(getStack(), maxDrain, doDrain);
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    public IFluidItemWrapper getItem() {
        return (IFluidItemWrapper) getStack().getItem();
    }

    @Override
    public boolean canProcess(Capability<?> cap) {
        return cap == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }
}
