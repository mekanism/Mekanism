package mekanism.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.client.render.obj.TransmitterModel;
import mekanism.client.voice.VoiceClient;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.network.PacketKey;
import mekanism.common.security.SecurityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

public class MekanismClient extends Mekanism {

    public static Map<UUID, SecurityData> clientSecurityMap = new HashMap<>();
    public static Map<UUID, String> clientUUIDMap = new HashMap<>();

    public static VoiceClient voiceClient;
    public static long ticksPassed = 0;

    public static void updateKey(KeyBinding key, int type) {
        boolean down = Minecraft.getInstance().currentScreen == null && key.isKeyDown();
        if (down != keyMap.has(Minecraft.getInstance().player, type)) {
            Mekanism.packetHandler.sendToServer(new PacketKey(type, down));
            keyMap.update(Minecraft.getInstance().player, type, down);
        }
    }

    public static void reset() {
        clientSecurityMap.clear();
        clientUUIDMap.clear();

        if (MekanismConfig.current().general.voiceServerEnabled.val()) {
            if (MekanismClient.voiceClient != null) {
                MekanismClient.voiceClient.disconnect();
                MekanismClient.voiceClient = null;
            }
        }

        ClientTickHandler.tickingSet.clear();
        ClientTickHandler.portableTeleports.clear();

        TransmitterModel.clearCache();

        MekanismAPI.getBoxIgnore().clear();
        MekanismAPI.getBoxModIgnore().clear();
        MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());

        Mekanism.playerState.clear();
        Mekanism.activeVibrators.clear();
        Mekanism.freeRunnerOn.clear();

        SynchronizedBoilerData.clientHotMap.clear();

        MekanismConfig.setSyncedConfig(null);
        Mekanism.proxy.onConfigSync(false);

        for (IModule module : Mekanism.modulesLoaded) {
            module.resetClient();
        }
    }
}