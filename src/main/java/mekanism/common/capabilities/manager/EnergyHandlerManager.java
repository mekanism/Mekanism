package mekanism.common.capabilities.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class EnergyHandlerManager extends CapabilityHandlerManager<IEnergyContainerHolder, IEnergyContainer, IStrictEnergyHandler, ISidedStrictEnergyHandler> {

    public EnergyHandlerManager(@Nullable IEnergyContainerHolder holder, @Nonnull ISidedStrictEnergyHandler baseHandler) {
        super(holder, baseHandler, ProxyStrictEnergyHandler::new, IEnergyContainerHolder::getEnergyContainers);
    }

    public EnergyHandlerManager(@Nullable IEnergyContainerHolder holder, boolean canHandle, @Nonnull ISidedStrictEnergyHandler baseHandler) {
        super(holder, canHandle, baseHandler, ProxyStrictEnergyHandler::new, IEnergyContainerHolder::getEnergyContainers);
    }

    @Nonnull
    public <T> LazyOptional<T> getEnergyCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (!canHandle() || getContainers(side).isEmpty()) {
            //If we can't handle this type or there are no containers accessible from that side, don't return a handler
            return LazyOptional.empty();
        }
        return EnergyCompatUtils.getEnergyCapability(capability, getHandler(side));
    }
}