package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IContainerCreator<CONTAINER extends INBTSerializable<CompoundTag>, ATTACHED extends IAttachedContainers<?, ATTACHED>> extends IBasicContainerCreator<CONTAINER> {

    int totalContainers();

    ATTACHED initStorage(int containers);
}