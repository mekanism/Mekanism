package mekanism.common.base;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;

/**
 * Internal interface used for blocks that send data between clients and the server
 * @author AidanBrady
 *
 */
public interface ITileNetwork
{
	/**
	 * Receive and manage a packet's data.
	 * @param dataStream
	 */
	public void handlePacketData(ByteBuf dataStream) throws Exception;

	/**
	 * Gets an ArrayList of data this tile entity keeps synchronized with the client.
	 * @param data - list of data
	 * @return ArrayList
	 */
	public ArrayList getNetworkedData(ArrayList data);
}
