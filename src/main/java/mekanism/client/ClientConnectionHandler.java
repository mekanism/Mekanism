package mekanism.client;

import java.net.InetSocketAddress;

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
				//If the client is connecting to its own corresponding integrated server.
				try {
					MekanismClient.voiceClient = new VoiceClient("127.0.0.1");
					//Will probably not work when multiple integrateds are running on one computer
				} catch(Exception e) {
					Mekanism.logger.error("Unable to establish VoiceClient on local connection.");
					e.printStackTrace();
				}
			}
			else {
				//If the client is connecting to a foreign integrated or dedicated server.
				try {
					MekanismClient.voiceClient = new VoiceClient(((InetSocketAddress)event.manager.getSocketAddress()).getHostString());
				} catch(Exception e) {
					Mekanism.logger.error("Unable to establish VoiceClient on remote connection.");
					e.printStackTrace();
				}
			}
		}
	}
}
