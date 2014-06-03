package mekanism.common;

import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData;
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
			Mekanism.packetPipeline.sendTo(new PacketConfigSync(), (EntityPlayerMP)event.player);
			Mekanism.packetPipeline.sendTo(new PacketBoxBlacklist(), (EntityPlayerMP)event.player);
			Mekanism.packetPipeline.sendTo(new PacketJetpackData(JetpackPacket.FULL, null, false), (EntityPlayerMP)event.player);
			Mekanism.packetPipeline.sendTo(new PacketScubaTankData(ScubaTankPacket.FULL, null, false), (EntityPlayerMP)event.player);

			Mekanism.logger.info("Sent config to '" + event.player.getDisplayName() + ".'");
		}
	}

	@SubscribeEvent
	public void onPlayerLogoutEvent(PlayerLoggedOutEvent event)
	{
		Mekanism.jetpackOn.remove(event.player);
		Mekanism.gasmaskOn.remove(event.player);
	}

	@SubscribeEvent
	public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event)
	{
		Mekanism.jetpackOn.remove(event.player);

		if(!event.player.worldObj.isRemote)
		{
			Mekanism.packetPipeline.sendTo(new PacketJetpackData(JetpackPacket.FULL, null, false), (EntityPlayerMP)event.player);
			Mekanism.packetPipeline.sendTo(new PacketScubaTankData(ScubaTankPacket.FULL, null, false), (EntityPlayerMP)event.player);
		}
	}
}
