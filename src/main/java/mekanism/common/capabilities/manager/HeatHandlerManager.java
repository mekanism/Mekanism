package mekanism.common.capabilities.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.proxy.ProxyHeatHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class HeatHandlerManager extends CapabilityHandlerManager<IHeatCapacitorHolder, IHeatCapacitor, IHeatHandler, ISidedHeatHandler> {

    public HeatHandlerManager(@Nullable IHeatCapacitorHolder holder, @Nonnull ISidedHeatHandler baseHandler) {
        super(holder, baseHandler, ProxyHeatHandler::new, IHeatCapacitorHolder::getHeatCapacitors);
    }

    public HeatHandlerManager(@Nullable IHeatCapacitorHolder holder, boolean canHandle, @Nonnull ISidedHeatHandler baseHandler) {
        super(holder, canHandle, baseHandler, ProxyHeatHandler::new, IHeatCapacitorHolder::getHeatCapacitors);
    }
}