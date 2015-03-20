package mekanism.common;

import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CommonPlayerTracker
{
	public CommonPlayerTracker()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onPlayerLoginEvent(PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote)
		{
			Mekanism.packetHandler.sendTo(new ConfigSyncMessage(), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new BoxBlacklistMessage(), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new JetpackDataMessage(JetpackPacket.FULL, null, false), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new ScubaTankDataMessage(ScubaTankPacket.FULL, null, false), (EntityPlayerMP)event.player);

			Mekanism.logger.info("Sent config to '" + event.player.getDisplayName() + ".'");
		}
	}

	@SubscribeEvent
	public void onPlayerLogoutEvent(PlayerLoggedOutEvent event)
	{
		Mekanism.jetpackOn.remove(event.player.getCommandSenderName());
		Mekanism.gasmaskOn.remove(event.player.getCommandSenderName());
		Mekanism.flamethrowerActive.remove(event.player.getCommandSenderName());
	}

	@SubscribeEvent
	public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event)
	{
		Mekanism.jetpackOn.remove(event.player.getCommandSenderName());
		Mekanism.gasmaskOn.remove(event.player.getCommandSenderName());
		Mekanism.flamethrowerActive.remove(event.player.getCommandSenderName());

		if(!event.player.worldObj.isRemote)
		{
			Mekanism.packetHandler.sendTo(new JetpackDataMessage(JetpackPacket.FULL, null, false), (EntityPlayerMP)event.player);
			Mekanism.packetHandler.sendTo(new ScubaTankDataMessage(ScubaTankPacket.FULL, null, false), (EntityPlayerMP)event.player);
		}
	}
}
