package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.network.PacketKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class MekanismClient {

    public static Map<UUID, SecurityData> clientSecurityMap = new Object2ObjectOpenHashMap<>();
    public static Map<UUID, String> clientUUIDMap = new Object2ObjectOpenHashMap<>();
    public static boolean renderHUD = true;

    public static long ticksPassed = 0;

    public static void updateKey(KeyBinding key, int type) {
        boolean down = Minecraft.getInstance().currentScreen == null && key.isKeyDown();
        if (down != Mekanism.keyMap.has(Minecraft.getInstance().player, type)) {
            Mekanism.packetHandler.sendToServer(new PacketKey(type, down));
            Mekanism.keyMap.update(Minecraft.getInstance().player, type, down);
        }
    }

    public static void reset() {
        clientSecurityMap.clear();
        clientUUIDMap.clear();

        ClientTickHandler.portableTeleports.clear();
        ClientTickHandler.firstTick = true;
        ClientTickHandler.visionEnhancement = false;

        Mekanism.playerState.clear();
        Mekanism.activeVibrators.clear();
        Mekanism.radiationManager.resetClient();
        SoundHandler.radiationSoundMap.clear();
        TransmitterNetworkRegistry.getInstance().clearClientNetworks();
        RenderTickHandler.prevRadiation = 0;

        for (IModule module : Mekanism.modulesLoaded) {
            module.resetClient();
        }
    }

    public static void launchClient() {
        for (IModule module : Mekanism.modulesLoaded) {
            module.launchClient();
        }
    }
}