package universalelectricity.prefab.network;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;

public interface IPacketReceiver
{
	/**
	 * Sends some data to the tile entity.
	 */
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream);
}
