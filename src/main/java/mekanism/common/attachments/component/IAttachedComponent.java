package mekanism.common.attachments.component;

import mekanism.common.tile.component.ITileComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IAttachedComponent<COMPONENT extends ITileComponent> extends INBTSerializable<CompoundTag> {

    default void copyFrom(HolderLookup.Provider provider, COMPONENT component) {
        deserializeNBT(provider, component.serialize(provider));
    }

    default void copyTo(HolderLookup.Provider provider, COMPONENT component) {
        CompoundTag configNBT = serializeNBT(provider);
        if (configNBT != null) {
            component.deserialize(configNBT, provider);
        }
    }
}