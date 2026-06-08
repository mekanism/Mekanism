package mekanism.common.component.containers.creator;

import net.neoforged.neoforge.transfer.access.ItemAccess;

@FunctionalInterface
public interface IBasicContainerCreator<CONTAINER> {

    CONTAINER create(ItemAccess attachedAccess, int containerIndex);
}