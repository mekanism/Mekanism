package universalelectricity.prefab.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

public interface IPacketReceiver
{
	/**
	 * Sends some data to the tile entity.
	 */
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream);
}
