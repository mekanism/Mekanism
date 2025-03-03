package mekanism.additions.client;

import io.netty.channel.local.LocalAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;
import net.minecraft.network.Connection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MekanismAdditions.MODID, dist = Dist.CLIENT)
public class AdditionsClient {

    private static VoiceClient voiceClient;

    public AdditionsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static void reset() {
        if (voiceClient != null) {
            voiceClient.disconnect();
            voiceClient = null;
        }
    }

    public static void launch(Connection connection) {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            SocketAddress address = connection.getRemoteAddress();
            //local connection
            if (address instanceof LocalAddress) {
                voiceClient = new VoiceClient("127.0.0.1");
                voiceClient.start();
                //remote connection
            } else if (address instanceof InetSocketAddress socketAddress) {
                voiceClient = new VoiceClient(socketAddress.getHostString());
                voiceClient.start();
            } else {
                Mekanism.logger.error("Unknown connection address detected, voice client will not launch.");
            }
        }
    }
}