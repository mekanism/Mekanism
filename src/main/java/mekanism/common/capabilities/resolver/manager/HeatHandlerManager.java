package mekanism.common.capabilities.resolver.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.proxy.ProxyHeatHandler;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class HeatHandlerManager extends CapabilityHandlerManager<IHeatCapacitorHolder, IHeatCapacitor, IHeatHandler, ISidedHeatHandler> {

    public HeatHandlerManager(@Nullable IHeatCapacitorHolder holder, @Nonnull ISidedHeatHandler baseHandler) {
        super(holder, baseHandler, Capabilities.HEAT_HANDLER_CAPABILITY, ProxyHeatHandler::new, IHeatCapacitorHolder::getHeatCapacitors);
    }
}