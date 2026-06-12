package mekanism.common.capabilities.resolver.manager;

import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.proxy.ProxyHeatHandler;
import mekanism.common.capabilities.resolver.BasicSingleContainerHandlerManager;

/// Helper class to make reading instead of having as messy generics
public class HeatHandlerManager extends BasicSingleContainerHandlerManager<IHeatCapacitor, IHeatHandler> {

    public HeatHandlerManager(ISingleContainerHolder<IHeatCapacitor> holder) {
        super(holder, Capabilities.HEAT, ProxyHeatHandler::new);
    }
}