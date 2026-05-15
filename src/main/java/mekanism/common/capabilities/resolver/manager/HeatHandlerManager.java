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
public class HeatHandlerManager extends CapabilityHandlerManager<IHeatCapacitorHolder, IHeatCapacitor, IHeatHandler> {

    public HeatHandlerManager(@Nullable IHeatCapacitorHolder holder, @NotNull ISidedHeatHandler baseHandler) {
        //TODO - 26.1: Evaluate if we want to change this to be more like the other things where the handler isn't implemented by the tile itself
        super(holder, Capabilities.HEAT, IHeatCapacitorHolder::getHeatCapacitors, (side, h) -> new ProxyHeatHandler(baseHandler, side, h));
    }
}