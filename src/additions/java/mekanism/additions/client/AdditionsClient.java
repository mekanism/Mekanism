package mekanism.additions.client;

import mekanism.additions.client.voice.VoiceClient;
import mekanism.additions.common.config.MekanismAdditionsConfig;

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