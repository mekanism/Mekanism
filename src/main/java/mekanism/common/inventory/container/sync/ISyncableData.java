package mekanism.common.inventory.container.sync;

import mekanism.common.network.container.property.PropertyData;

public interface ISyncableData {

    boolean isDirty();

    PropertyData getPropertyData(short property);
}