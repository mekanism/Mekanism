package mekanism.common;

import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CommonPlayerTracker
{
	public CommonPlayerTracker()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onPlayerLoginEvent(PlayerLoggedInEvent event)
	{
		if(!event.player.world.isRemote)
		{
			Mekanism.packetHandler.sendTo(new ConfigSyncMessage(), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new BoxBlacklistMessage(), (EntityPlayerMP)event.player);
			// TODO: Coalesce all these sync events into a single message
			Mekanism.packetHandler.sendTo(JetpackDataMessage.FULL(Mekanism.playerState.getActiveJetpacks()), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(ScubaTankDataMessage.FULL(Mekanism.playerState.getActiveGasmasks()), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new SecurityUpdateMessage(SecurityPacket.FULL, null, null), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new PacketFreeRunnerData.FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.FULL, null, false), (EntityPlayerMP)event.player);

			Mekanism.logger.info("Sent config to '" + event.player.getDisplayNameString() + ".'");
		}
	}

	@SubscribeEvent
	public void onPlayerLogoutEvent(PlayerLoggedOutEvent event)
	{
		Mekanism.playerState.clearPlayer(event.player);
		Mekanism.freeRunnerOn.remove(event.player.getName());
	}

	@SubscribeEvent
	public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event)
	{
		Mekanism.playerState.clearPlayer(event.player);
		Mekanism.freeRunnerOn.remove(event.player.getName());

		if(!event.player.world.isRemote)
		{
			Mekanism.packetHandler.sendTo(JetpackDataMessage.FULL(Mekanism.playerState.getActiveJetpacks()), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(ScubaTankDataMessage.FULL(Mekanism.playerState.getActiveGasmasks()), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new PacketFreeRunnerData.FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.FULL, null, false), (EntityPlayerMP)event.player);
		}
	}
}
