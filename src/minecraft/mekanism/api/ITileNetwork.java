package mekanism.api;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

/**
 * Implement this in your TileEntity class if you plan to have your machine send and receive packets. Send packets sparingly!
 * @author AidanBrady
 *
 */
public interface ITileNetwork 
{
	/**
	 * Called when a networked machine receives a packet.
	 * @param network
	 * @param packet
	 * @param player
	 * @param dataStream
	 */
	public void handlePacketData(ByteArrayDataInput dataStream) throws Exception;
	
	/**
	 * Gets an ArrayList of data this machine keeps synchronized with the client.
	 * @param data - list of data
	 * @return ArrayList
	 */
	public ArrayList getNetworkedData(ArrayList data);
}
