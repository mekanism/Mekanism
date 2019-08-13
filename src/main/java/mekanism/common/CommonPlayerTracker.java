package mekanism.common;

import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTracker {

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        MinecraftServer server = event.getPlayer().getServer();
        if (!event.getPlayer().world.isRemote) {
            if (server == null || !server.isSinglePlayer()) {
                Mekanism.packetHandler.sendTo(new PacketConfigSync(MekanismConfigOld.local()), (ServerPlayerEntity) event.getPlayer());
                Mekanism.logger.info("Sent config to '" + event.getPlayer().getDisplayNameString() + ".'");
            }
            Mekanism.packetHandler.sendTo(new PacketBoxBlacklist(), (ServerPlayerEntity) event.getPlayer());
            syncChangedData((ServerPlayerEntity) event.getPlayer());
            Mekanism.packetHandler.sendTo(new PacketSecurityUpdate(SecurityPacket.FULL, null, null), (ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        Mekanism.playerState.clearPlayer(event.getPlayer().getUniqueID());
        Mekanism.freeRunnerOn.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        Mekanism.playerState.clearPlayer(event.getPlayer().getUniqueID());
        Mekanism.freeRunnerOn.remove(event.getPlayer().getUniqueID());
        if (!event.getPlayer().world.isRemote) {
            syncChangedData((ServerPlayerEntity) event.getPlayer());
        }
    }

    private void syncChangedData(ServerPlayerEntity player) {
        // TODO: Coalesce all these sync events into a single message
        Mekanism.packetHandler.sendTo(PacketJetpackData.FULL(Mekanism.playerState.getActiveJetpacks()), player);
        Mekanism.packetHandler.sendTo(PacketScubaTankData.FULL(Mekanism.playerState.getActiveGasmasks()), player);
        Mekanism.packetHandler.sendTo(PacketFlamethrowerData.FULL(Mekanism.playerState.getActiveFlamethrowers()), player);
        Mekanism.packetHandler.sendTo(new PacketFreeRunnerData(PacketFreeRunnerData.FreeRunnerPacket.FULL, null, false), player);
    }
}