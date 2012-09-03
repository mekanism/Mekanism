package net.uberkat.obsidian.common;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

/**
 * Implement this in your TileEntity class if you plan to have your machine send and receive packets. Send packets sparingly!
 * @author AidanBrady
 *
 */
public interface INetworkedMachine 
{
	/**
	 * Called when a networked machine receives a packet.
	 * @param network
	 * @param packet
	 * @param player
	 * @param dataStream
	 */
	public void handlePacketData(NetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream);
}
