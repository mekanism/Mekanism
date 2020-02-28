package mekanism.additions.client;

import io.netty.channel.local.LocalAddress;
import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkEvent.LoginPayloadEvent;

@Mod.EventBusSubscriber(modid = MekanismAdditions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VoiceClientRegistration {
    @SubscribeEvent
    public static void playerLoggedIn(PlayerLoggedInEvent event) {
        if (event.getPlayer().isServerWorld()) {
            System.out.println(Minecraft.getInstance().getConnection().getNetworkManager().getRemoteAddress().getClass());
            if (MekanismAdditionsConfig.additions.voiceServerEnabled.get() && AdditionsClient.voiceClient != null) {
                AdditionsClient.voiceClient.start();
            }
            System.out.println("Client logged in");
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerLoggedOutEvent event) {
        if (event.getPlayer().isServerWorld()) {
            System.out.println("Client logged out");
        }
    }

    @SubscribeEvent
    public static void onConnection(LoginPayloadEvent event) {
        /*if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (event.isLocal())  {
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
        }*/
    }
}
