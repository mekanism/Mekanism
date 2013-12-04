package mekanism.client;

import java.net.InetAddress;

import mekanism.client.voice.VoiceClient;
import mekanism.common.Mekanism;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientConnectionHandler implements IConnectionHandler
{
	public VoiceClient voiceClient;
	
	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) 
	{
		return null;
	}

	/* Remote */
	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) 
	{
		if(Mekanism.voiceServerEnabled)
		{
			try {
				voiceClient = new VoiceClient(server, Mekanism.VOICE_PORT);
				voiceClient.run();
			} catch(Exception e) {}
		}
	}

	/* Integrated */
	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) 
	{
		if(Mekanism.voiceServerEnabled)
		{
			try {
				voiceClient = new VoiceClient(InetAddress.getLocalHost().getHostAddress(), Mekanism.VOICE_PORT);
				voiceClient.run();
			} catch(Exception e) {}
		}
	}

	@Override
	public void connectionClosed(INetworkManager manager) 
	{
		if(Mekanism.voiceServerEnabled)
		{
			if(voiceClient != null)
			{
				voiceClient.disconnect();
				voiceClient = null;
			}
		}
		
		ClientTickHandler.tickingSet.clear();
		Mekanism.jetpackOn.clear();
		Mekanism.proxy.unloadSoundHandler();
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {}
}
