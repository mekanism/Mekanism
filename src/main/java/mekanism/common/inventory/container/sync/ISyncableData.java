package mekanism.common.inventory.container.sync;

import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.core.RegistryAccess;

public interface ISyncableData {

    DirtyType isDirty();

    //DirtyType will either be DIRTY or SIZE
    PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType);

    enum DirtyType {
        CLEAN,
        SIZE,
        DIRTY;

        public static DirtyType get(boolean dirty) {
            return dirty ? DIRTY : CLEAN;
        }
    }
}