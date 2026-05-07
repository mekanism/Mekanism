package mekanism.common.capabilities.resolver.manager;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class FluidHandlerManager extends CapabilityHandlerManager<IFluidTankHolder, IFluidTank, ResourceHandler<FluidResource>> {

    public FluidHandlerManager(@Nullable IFluidTankHolder holder, @NotNull IContentsListener changeListener) {
        super(holder, Capabilities.FLUID.block(), IFluidTankHolder::getTanks, (side, h) -> new ProxyResourceHandler<>(new IMekanismFluidHandler() {
            @Override
            public void onContentsChanged() {
                changeListener.onContentsChanged();
            }

            @Override
            public List<IFluidTank> getFluidTanks(@Nullable Direction side) {
                return getContainers();
            }

            @NotNull
            @Override
            public List<IFluidTank> getContainers() {
                //Note: This instance of check should always pass, but we have it in case we are passed a null holder
                return h instanceof IFluidTankHolder tankHolder ? tankHolder.getTanks(side) : Collections.emptyList();
            }
        }, side, h));
    }
}