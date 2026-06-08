package mekanism.common.component;

import net.neoforged.bus.api.IEventBus;

@FunctionalInterface
public interface IComponentAware {

    void addComponents(IEventBus eventBus);
}