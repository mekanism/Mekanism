package mekanism.common;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

/**
 * Internal interface used for blocks that send data between clients and the server
 * @author AidanBrady
 *
 */
public interface ITileNetwork
{
	/**
	 * Receive and manage a data input.
	 * @param network
	 * @param packet
	 * @param player
	 * @param dataStream
	 */
	public void handlePacketData(ByteArrayDataInput dataStream) throws Exception;

	/**
	 * Gets an ArrayList of data this tile entity keeps synchronized with the client.
	 * @param data - list of data
	 * @return ArrayList
	 */
	public ArrayList getNetworkedData(ArrayList data);
}
