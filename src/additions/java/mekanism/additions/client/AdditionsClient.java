package mekanism.additions.client;

import io.netty.channel.local.LocalAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;

public class AdditionsClient {

    private AdditionsClient() {
    }

    private static VoiceClient voiceClient;

    public static void reset() {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get() && voiceClient != null) {
            voiceClient.disconnect();
            voiceClient = null;
        }
    }

    public static void launch() {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            SocketAddress address = Minecraft.getInstance().getConnection().getNetworkManager().getRemoteAddress();
            //local connection
            if (address instanceof LocalAddress) {
                voiceClient = new VoiceClient("127.0.0.1");
                AdditionsClient.voiceClient.start();
                //remote connection
            } else if (address instanceof InetSocketAddress) {
                voiceClient = new VoiceClient(((InetSocketAddress) address).getHostString());
                AdditionsClient.voiceClient.start();
            } else {
                Mekanism.logger.error("Unknown connection address detected, voice client will not launch.");
            }
        }
    }
}