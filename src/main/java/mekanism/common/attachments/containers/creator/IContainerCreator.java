package mekanism.common.attachments.containers.creator;

import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

//TODO - 1.20.5: Do we want to require the nbt serializable bound on the container?
//TODO - 1.20.5: Better generic names once we figure out the methods and stuff we need
public interface IContainerCreator<CONTAINER extends INBTSerializable<CompoundTag>, ATTACHED extends IAttachedContainers<?, ATTACHED>> extends IBasicContainerCreator<CONTAINER> {

    int totalContainers();

    ATTACHED initStorage(int containers);
}