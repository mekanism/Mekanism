package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.base.IModule;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.network.PacketKey;
import mekanism.common.security.SecurityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

//TODO: Figure out why this extends Mekanism, given it only adds static methods
public class MekanismClient {

    public static Map<UUID, SecurityData> clientSecurityMap = new Object2ObjectOpenHashMap<>();
    public static Map<UUID, String> clientUUIDMap = new Object2ObjectOpenHashMap<>();

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

        ClientTickHandler.tickingSet.clear();
        ClientTickHandler.portableTeleports.clear();

        Mekanism.playerState.clear();
        Mekanism.activeVibrators.clear();
        Mekanism.freeRunnerOn.clear();

        SynchronizedBoilerData.clientHotMap.clear();

        for (IModule module : Mekanism.modulesLoaded) {
            module.resetClient();
        }
    }
}