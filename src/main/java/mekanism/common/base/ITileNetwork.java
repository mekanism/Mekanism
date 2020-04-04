package mekanism.common.base;

import net.minecraft.network.PacketBuffer;

/**
 * Internal interface used for blocks that send data between clients and the server
 *
 * @author AidanBrady
 */
@Deprecated
public interface ITileNetwork {

    /**
     * Receive and manage a packet's data.
     *
     * @param dataStream Datastream to parse
     */
    void handlePacketData(PacketBuffer dataStream);
}