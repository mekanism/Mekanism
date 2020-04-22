package mekanism.common.capabilities.resolver.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.ISidedFluidHandler;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.proxy.ProxyFluidHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class FluidHandlerManager extends CapabilityHandlerManager<IFluidTankHolder, IExtendedFluidTank, IFluidHandler, ISidedFluidHandler> {

    public FluidHandlerManager(@Nullable IFluidTankHolder holder, @Nonnull ISidedFluidHandler baseHandler) {
        super(holder, baseHandler, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ProxyFluidHandler::new, IFluidTankHolder::getTanks);
    }
}