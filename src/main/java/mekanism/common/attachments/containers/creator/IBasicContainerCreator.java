package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

@FunctionalInterface
public interface IBasicContainerCreator<CONTAINER extends ValueIOSerializable> {

    CONTAINER create(ContainerType<? super CONTAINER, ?, ?> containerType, ItemStack attachedTo, int containerIndex);
}