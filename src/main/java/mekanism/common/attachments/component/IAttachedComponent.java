package mekanism.common.attachments.component;

import mekanism.common.tile.component.ITileComponent;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IAttachedComponent<COMPONENT extends ITileComponent> extends INBTSerializable<CompoundTag> {

    default void copyFrom(COMPONENT component) {
        deserializeNBT(component.serialize());
    }

    default void copyTo(COMPONENT component) {
        CompoundTag configNBT = serializeNBT();
        if (configNBT != null) {
            component.deserialize(configNBT);
        }
    }
}