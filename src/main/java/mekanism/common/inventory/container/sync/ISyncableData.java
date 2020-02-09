package mekanism.common.inventory.container.sync;

import mekanism.common.network.container.PacketUpdateContainer;

public interface ISyncableData<PACKET extends PacketUpdateContainer<PACKET>> {

    boolean isDirty();

    PacketUpdateContainer<PACKET> getUpdatePacket(short windowId, short property);
}