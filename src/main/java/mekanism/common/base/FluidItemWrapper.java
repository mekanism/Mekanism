package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidItemWrapper extends ItemCapability implements IFluidHandlerItem {

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        //TODO: Multiple tanks/check the tank
        return getItem().getFluid(getStack());
    }

    @Override
    public int getTankCapacity(int tank) {
        return getItem().getCapacity(getStack());
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        FluidStack fluidInTank = getFluidInTank(tank);
        //TODO: Better check?
        return fluidInTank.isEmpty() || fluidInTank.isFluidEqual(stack);
    }

    @Override
    public int fill(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (getStack().getCount() != 1) {
            return 0;
        }
        return getItem().fill(getStack(), resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (getStack().getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack canDrain = drain(resource.getAmount(), FluidAction.SIMULATE);
        if (!canDrain.isEmpty() && canDrain.isFluidEqual(resource)) {
            return drain(resource.getAmount(), fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction fluidAction) {
        if (getStack().getCount() != 1) {
            return FluidStack.EMPTY;
        }
        return getItem().drain(getStack(), maxDrain, fluidAction);
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