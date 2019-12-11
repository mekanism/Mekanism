package mekanism.additions.client;

import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.GatherLoginPayloadsEvent;

public class ClientConnectionHandler {

    @SubscribeEvent
    public void onConnection(GatherLoginPayloadsEvent event) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (event.isLocal()) {
                // If the client is connecting to its own corresponding integrated server.
                try {
                    AdditionsClient.voiceClient = new VoiceClient("127.0.0.1");
                    // Will probably not work when multiple integrateds are running on one computer
                    AdditionsClient.voiceClient.start();//start here as config sync is not sent
                } catch (Throwable e) {
                    Mekanism.logger.error("Unable to establish VoiceClient on local connection.", e);
                }
            } else {
                // If the client is connecting to a foreign integrated or dedicated server.
                try {
                    //TODO: Get remote address
                    //AdditionsClient.voiceClient = new VoiceClient(((InetSocketAddress) event.getManager().getRemoteAddress()).getHostString());
                } catch (Throwable e) {
                    Mekanism.logger.error("Unable to establish VoiceClient on remote connection.", e);
                }
            }
        }
    }
}