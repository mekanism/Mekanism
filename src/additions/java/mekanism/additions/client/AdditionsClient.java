package mekanism.additions.client;

import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.network.NetworkEvent.GatherLoginPayloadsEvent;

public class AdditionsClient {

    public static VoiceClient voiceClient;

    public static void reset() {
        if (MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            if (voiceClient != null) {
                voiceClient.disconnect();
                voiceClient = null;
            }
        }
    }
}