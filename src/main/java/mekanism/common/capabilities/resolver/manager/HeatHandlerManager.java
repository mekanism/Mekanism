package mekanism.common.capabilities.resolver.manager;

import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.ISidedHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.proxy.ProxyHeatHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
public class HeatHandlerManager extends CapabilityHandlerManager<IHeatCapacitorHolder, IHeatCapacitor, IHeatHandler, ISidedHeatHandler> {

    public HeatHandlerManager(@Nullable IHeatCapacitorHolder holder, @NotNull ISidedHeatHandler baseHandler) {
        super(holder, baseHandler, Capabilities.HEAT, ProxyHeatHandler::new, IHeatCapacitorHolder::getHeatCapacitors);
    }
}