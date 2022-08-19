package mekanism.common.capabilities.resolver.manager;

import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.ISidedFluidHandler;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.proxy.ProxyFluidHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class FluidHandlerManager extends CapabilityHandlerManager<IFluidTankHolder, IExtendedFluidTank, IFluidHandler, ISidedFluidHandler> {

    public FluidHandlerManager(@Nullable IFluidTankHolder holder, @NotNull ISidedFluidHandler baseHandler) {
        super(holder, baseHandler, ForgeCapabilities.FLUID_HANDLER, ProxyFluidHandler::new, IFluidTankHolder::getTanks);
    }
}