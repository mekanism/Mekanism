package mekanism.client;

import java.net.InetSocketAddress;
import mekanism.client.voice.VoiceClient;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientConnectionHandler {

    @SubscribeEvent
    public void onConnection(ClientConnectedToServerEvent event) {
        if (MekanismConfig.current().general.voiceServerEnabled.val()) {
            if (event.isLocal()) {
                // If the client is connecting to its own corresponding integrated server.
                try {
                    MekanismClient.voiceClient = new VoiceClient("127.0.0.1");
                    // Will probably not work when multiple integrateds are running on one computer
                    MekanismClient.voiceClient.start();//start here as config sync is not sent
                } catch (Throwable e) {
                    Mekanism.logger.error("Unable to establish VoiceClient on local connection.", e);
                }
            } else {
                // If the client is connecting to a foreign integrated or dedicated server.
                try {
                    MekanismClient.voiceClient = new VoiceClient(((InetSocketAddress) event.getManager().getRemoteAddress()).getHostString());
                } catch (Throwable e) {
                    Mekanism.logger.error("Unable to establish VoiceClient on remote connection.", e);
                }
            }
        }
    }
}