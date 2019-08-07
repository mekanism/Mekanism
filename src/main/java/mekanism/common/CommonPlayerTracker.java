package mekanism.common;

import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CommonPlayerTracker {

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        MinecraftServer server = event.player.getServer();
        if (!event.player.world.isRemote) {
            if (server == null || !server.isSinglePlayer()) {
                Mekanism.packetHandler.sendTo(new PacketConfigSync(MekanismConfig.local()), (ServerPlayerEntity) event.player);
                Mekanism.logger.info("Sent config to '" + event.player.getDisplayNameString() + ".'");
            }
            Mekanism.packetHandler.sendTo(new PacketBoxBlacklist(), (ServerPlayerEntity) event.player);
            syncChangedData((ServerPlayerEntity) event.player);
            Mekanism.packetHandler.sendTo(new SecurityUpdateMessage(SecurityPacket.FULL, null, null), (ServerPlayerEntity) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        Mekanism.playerState.clearPlayer(event.player.getUniqueID());
        Mekanism.freeRunnerOn.remove(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        Mekanism.playerState.clearPlayer(event.player.getUniqueID());
        Mekanism.freeRunnerOn.remove(event.player.getUniqueID());
        if (!event.player.world.isRemote) {
            syncChangedData((ServerPlayerEntity) event.player);
        }
    }

    private void syncChangedData(ServerPlayerEntity player) {
        // TODO: Coalesce all these sync events into a single message
        Mekanism.packetHandler.sendTo(PacketJetpackData.FULL(Mekanism.playerState.getActiveJetpacks()), player);
        Mekanism.packetHandler.sendTo(ScubaTankDataMessage.FULL(Mekanism.playerState.getActiveGasmasks()), player);
        Mekanism.packetHandler.sendTo(PacketFlamethrowerData.FULL(Mekanism.playerState.getActiveFlamethrowers()), player);
        Mekanism.packetHandler.sendTo(new PacketFreeRunnerData(PacketFreeRunnerData.FreeRunnerPacket.FULL, null, false), player);
    }
}