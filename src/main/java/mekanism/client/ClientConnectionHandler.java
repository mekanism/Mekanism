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
		if(Mekanism.voiceServerEnabled)
		{
			if(event.isLocal)
			{
				try {
					MekanismClient.voiceClient = new VoiceClient(InetAddress.getLocalHost().getHostAddress());
				} catch(Exception e) {
					Mekanism.logger.error("Unable to establish VoiceClient on local connection.");
					e.printStackTrace();
				}
			}
			else {
				try {
					MekanismClient.voiceClient = new VoiceClient(event.manager.getSocketAddress().toString());
				} catch(Exception e) {
					Mekanism.logger.error("Unable to establish VoiceClient on remote connection.");
					e.printStackTrace();
				}
				//TODO this is probably wrong
			}
		}
	}
}
