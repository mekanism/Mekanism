package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.ContainerType;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@FunctionalInterface
public interface IBasicContainerCreator<CONTAINER extends ValueIOSerializable> {

    CONTAINER create(ContainerType<? super CONTAINER, ?, ?> containerType, ItemAccess attachedAccess, int containerIndex);
}