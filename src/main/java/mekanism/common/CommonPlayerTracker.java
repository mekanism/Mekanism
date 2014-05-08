package mekanism.common;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackPacket;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CommonPlayerTracker
{
	@SubscribeEvent
	public void onPlayerLoginEvent(PlayerLoggedInEvent event)
	{
		onPlayerLogin(event.player);
	}

	public void onPlayerLogin(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			Mekanism.packetPipeline.sendTo(new PacketConfigSync(), player);
			Mekanism.packetPipeline.sendTo(new PacketBoxBlacklist(), player);
			Mekanism.packetPipeline.sendTo(new PacketJetpackData(JetpackPacket.FULL), player);
			Mekanism.packetPipeline.sendTo(new PacketScubaTankData(ScubaTankPacket.FULL), player);

			Mekanism.logger.info((String) "Sent config to '" + player.getDisplayName() + ".'");
		}
	}

	@SubscribeEvent
	public void onPlayerLogoutEvent(PlayerLoggedOutEvent event)
	{
		onPlayerLogout(event.player);
	}

	public void onPlayerLogout(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);
		Mekanism.gasmaskOn.remove(player);
	}

	@SubscribeEvent
	public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event)
	{
		onPlayerChangedDimension(event.player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		Mekanism.jetpackOn.remove(player);

		if(!player.worldObj.isRemote)
		{
			Mekanism.packetPipeline.sendTo(new PacketJetpackData(JetpackPacket.FULL), player);
			Mekanism.packetPipeline.sendTo(new PacketScubaTankData(ScubaTankPacket.FULL), player);
		}
	}
}
