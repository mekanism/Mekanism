package mekanism.client;

import java.net.InetAddress;

import mekanism.client.voice.VoiceClient;
import mekanism.common.Mekanism;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientConnectionHandler
{
	@SubscribeEvent
	public void onConnection(ClientConnectedToServerEvent event)
	{
		if(event.isLocal)
		{
			connectionOpened();
		}
		else
		{
			//TODO this is probably wrong
			connectionOpened(event.manager.getSocketAddress().toString());
		}
	}

	/* Remote */
	public void connectionOpened(String server)
	{
		if(Mekanism.voiceServerEnabled)
		{
			try {
				MekanismClient.voiceClient = new VoiceClient(server);
			} catch(Exception e) {}
		}
	}

	/* Integrated */
	public void connectionOpened()
	{
		if(Mekanism.voiceServerEnabled)
		{
			try {
				MekanismClient.voiceClient = new VoiceClient(InetAddress.getLocalHost().getHostAddress());
			} catch(Exception e) {}
		}
	}

}
