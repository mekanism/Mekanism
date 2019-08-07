package mekanism.common.base;

import mekanism.api.TileNetworkList;
import net.minecraft.network.PacketBuffer;

/**
 * Internal interface used for blocks that send data between clients and the server
 *
 * @author AidanBrady
 */
public interface ITileNetwork {

    /**
     * Receive and manage a packet's data.
     *
     * @param dataStream Datastream to parse
     */
    void handlePacketData(PacketBuffer dataStream) throws Exception;

    /**
     * Gets an ArrayList of data this tile entity keeps synchronized with the client.
     *
     * @param data - list of data
     *
     * @return ArrayList of network data
     */
    TileNetworkList getNetworkedData(TileNetworkList data);

    /**
     * Gets an ArrayList of data this tile entity keeps synchronized with the client.
     *
     * @return ArrayList of network data
     */
    default TileNetworkList getNetworkedData() {
        return getNetworkedData(new TileNetworkList());
    }
}